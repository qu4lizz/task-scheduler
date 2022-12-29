package qu4lizz.taskscheduler.my_task;

import qu4lizz.taskscheduler.task.UserTask;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;


public class EdgeDetectionTask extends UserTask {
    private BufferedImage[] sourceImages;
    private final String outputDir;
    private final AtomicBoolean didRead = new AtomicBoolean(true);
    private Thread[] threads;

    public EdgeDetectionTask(String name, int numOfThreads, String[] paths, String outputDir) throws IOException, InterruptedException {
        super(name, numOfThreads);
        readImages(numOfThreads, paths);
        this.outputDir = outputDir;
    }

    public EdgeDetectionTask(String name, int priority, int numOfThreads, String[] paths, String outputDir) throws IOException, InterruptedException {
        super(name, priority, numOfThreads);
        readImages(numOfThreads, paths);
        this.outputDir = outputDir;
    }

    public EdgeDetectionTask(String name, String startDate, int numOfThreads, String[] paths, String outputDir) throws IOException, InterruptedException {
        super(name, startDate, numOfThreads);
        readImages(numOfThreads, paths);
        this.outputDir = outputDir;
    }

    public EdgeDetectionTask(String name, int priority, String startDate, int numOfThreads, String[] paths, String outputDir) throws IOException, InterruptedException {
        super(name, priority, startDate, numOfThreads);
        readImages(numOfThreads, paths);
        this.outputDir = outputDir;
    }

    public EdgeDetectionTask(String name, String startDate, String endDate, int numOfThreads, String[] paths, String outputDir) throws IOException, InterruptedException {
        super(name, startDate, endDate, numOfThreads);
        readImages(numOfThreads, paths);
        this.outputDir = outputDir;
    }

    public EdgeDetectionTask(String name, int priority, String startDate, String endDate, int numOfThreads, String[] paths, String outputDir) throws IOException, InterruptedException {
        super(name, priority, startDate, endDate, numOfThreads);
        readImages(numOfThreads, paths);
        this.outputDir = outputDir;
    }

    public EdgeDetectionTask(String name, String startDate, int time, int numOfThreads, String[] paths, String outputDir) throws IOException, InterruptedException {
        super(name, startDate, time, numOfThreads);
        readImages(numOfThreads, paths);
        this.outputDir = outputDir;
    }

    public EdgeDetectionTask (String name, int priority, String startDate, int time, int numOfThreads, String[] paths, String outputDir) throws IOException, InterruptedException {
        super(name, priority, startDate, time, numOfThreads);
        this.outputDir = outputDir;
        readImages(numOfThreads, paths);
    }

    private void readImages(int numOfThreads, String[] paths) throws IOException, InterruptedException {
        threads = new Thread[numOfThreads];
        sourceImages = new BufferedImage[paths.length];
        int used = 0;
        for(int i = 0, k = 0; k < numOfThreads; k++) {
            final int index = i;
            final int end = k == numOfThreads - 1 ? paths.length : i + (int)Math.ceil((double) paths.length / numOfThreads);
            threads[k] = new Thread(() -> {
                try {
                    for(int j = index; j < end; j++) {
                        sourceImages[j] = ImageIO.read(new File(paths[j]));
                    }
                } catch (IOException ignore) {
                    didRead.set(false);
                }
            });
            used++;
            threads[k].start();
            i = end;
            if (end == paths.length)
                break;
        }
        if (!didRead.get()) {
            throw new IOException("Failed to read image");
        }
        for(int i = 0; i < used; i++) {
            threads[i].join();
        }
    }

    public void execute() {
        long chunk = calculateChunk();
        long currentChunk;
        ArrayList<BufferedImage> chunkedImages = new ArrayList<>();
        Map<Integer, ArrayList<Integer>> imagePieces = new LinkedHashMap<>();
        Map<Integer, ArrayList<Integer>> imagesPerThread = new LinkedHashMap<>();
        int x = 0, y = 0, width, height, leftInChunk = 0, threadIndex = 0;
        for(int currSrc = 0, currChnkd = 0; currSrc < sourceImages.length; ) {
            currentChunk = 0;
            while (chunk > currentChunk && currSrc < sourceImages.length) {
                width = sourceImages[currSrc].getWidth();
                height = sourceImages[currSrc].getHeight();
                long totalPixels = (long) width * height;
                if (chunk + leftInChunk >= totalPixels + currentChunk) {
                    chunkedImages.add(sourceImages[currSrc]);
                    if (imagePieces.get(currSrc) == null)
                        imagePieces.put(currSrc, new ArrayList<>());
                    imagePieces.get(currSrc).add(currChnkd);
                    if (imagesPerThread.get(threadIndex) == null)
                        imagesPerThread.put(threadIndex, new ArrayList<>());
                    imagesPerThread.get(threadIndex).add(currChnkd);
                    currChnkd++;
                    currSrc++;
                    currentChunk += totalPixels;
                    if (chunk < currentChunk)
                        leftInChunk += (int) (chunk - currentChunk);
                    x = y = 0;
                } else {
                    int x1 = width;
                    int y1 = Math.min((int) (chunk / x1), sourceImages[currSrc].getHeight() - y);
                    chunkedImages.add(sourceImages[currSrc].getSubimage(x, y, x1, y1));
                    if (imagePieces.get(currSrc) == null)
                        imagePieces.put(currSrc, new ArrayList<>());
                    imagePieces.get(currSrc).add(currChnkd);
                    if (imagesPerThread.get(threadIndex) == null)
                        imagesPerThread.put(threadIndex, new ArrayList<>());
                    imagesPerThread.get(threadIndex).add(currChnkd);
                    currChnkd++;
                    currentChunk += (long) x1 * y1;
                    if (y + y1 >= sourceImages[currSrc].getHeight()) {
                        x = y = 0;
                        currSrc++;
                        if (currSrc < sourceImages.length && chunk < currentChunk + sourceImages[currSrc].getHeight() * sourceImages[currSrc].getWidth()) {
                            leftInChunk += (int) (chunk - currentChunk);
                            threadIndex++;
                            break;
                        }
                    } else {
                        x = 0;
                        y += y1;
                        leftInChunk += (int) (chunk - currentChunk);
                        threadIndex++;
                        break;
                    }
                }

            }
        }
        BufferedImage[] chunkedResult = getProcessedImages(imagesPerThread, chunkedImages);
        BufferedImage[] result = new BufferedImage[sourceImages.length];
        for(int i = 0; i < sourceImages.length; i++) {
            BufferedImage[] imagePiece = new BufferedImage[imagePieces.get(i).size()];
            for(int j = 0; j < imagePieces.get(i).size(); j++) {
                imagePiece[j] = chunkedResult[imagePieces.get(i).get(j)];
            }
            result[i] = concatenateImages(imagePiece);
        }
        new File(outputDir).mkdir();
        for (int i = 0; i < result.length; i++) {
            try {
                ImageIO.write(result[i], "jpg", new File(outputDir + "/img" + i + ".jpg"));
            } catch (IOException ignore) { }
        }
    }

    private long calculateChunk () {
        int sum = 0;
        for(BufferedImage image : sourceImages)
            sum += image.getHeight() * image.getWidth();
        return (long) Math.ceil((double) sum / threads.length);
    }

    private BufferedImage[] detectEdges (BufferedImage[] images) {
        BufferedImage[] result = new BufferedImage[images.length];
        for(int i = 0; i < images.length; i++) {
            result[i] = detectEdges(images[i]);
        }
        return result;
    }

    private BufferedImage detectEdges (BufferedImage image) {
        int[][] pixels = getPixels(image);
        int[][] grayscale = toGrayscale(pixels);
        int[][] blur = applyGaussianBlur(grayscale, 3);
        int[][] edges = getEdges(blur);
        return getImage(edges, image.getType());
    }

    private BufferedImage[] getProcessedImages (Map<Integer, ArrayList<Integer>> imagesPerThread, ArrayList<BufferedImage> chunkedImages) {
        BufferedImage[] chunkedResult = new BufferedImage[chunkedImages.size()];
        AtomicReferenceArray<BufferedImage[]> tmp = new AtomicReferenceArray<>(threads.length);
        for(int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                BufferedImage[] images = new BufferedImage[imagesPerThread.get(index).size()];
                for(int j = 0; j < images.length; j++) {
                    images[j] = chunkedImages.get(imagesPerThread.get(index).get(j));
                }
                tmp.set(index, detectEdges(images));
            });
            threads[i].start();
        }
        for(var thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignore) { }
        }
        for(int i = 0, rf = 0; i < tmp.length(); i++) {
            for(int j = 0; j < tmp.get(i).length; j++) {
                chunkedResult[rf + j] = tmp.get(i)[j];
            }
            rf += tmp.get(i).length;
        }
        return chunkedResult;
    }

    private static int[][] getPixels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] pixels = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x][y] = image.getRGB(x, y);
            }
        }
        return pixels;
    }

    private static int[][] toGrayscale(int[][] pixels) {
        int width = pixels.length;
        int height = pixels[0].length;
        int[][] grayscale = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = pixels[x][y];
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int gray = (int) (0.2989 * r + 0.5870 * g + 0.1140 * b);
                grayscale[x][y] = (gray << 16) | (gray << 8) | gray;
            }
        }
        return grayscale;
    }

    private int[][] applyGaussianBlur(int[][] pixels, int kernelSize) {
        int width = pixels.length;
        int height = pixels[0].length;
        int[][] blur = new int[width][height];
        double[][] kernel = getGaussianKernel(kernelSize);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double sum = 0;
                for (int i = -kernelSize / 2; i <= kernelSize / 2; i++) {
                    for (int j = -kernelSize / 2; j <= kernelSize / 2; j++) {
                        double kernelValue = kernel[i + kernelSize / 2][j + kernelSize / 2];
                        int pixelX = clamp(x + i, 0, width - 1);
                        int pixelY = clamp(y + j, 0, height - 1);
                        int pixel = pixels[pixelX][pixelY];
                        sum += kernelValue * (pixel & 0xff);
                    }
                }
                blur[x][y] = (int) sum;
            }
        }
        return blur;
    }

    private double[][] getGaussianKernel(int kernelSize) {
        double[][] kernel = new double[kernelSize][kernelSize];
        double sum = 0;
        double sigma = kernelSize / 3.0;
        for (int x = 0; x < kernelSize; x++) {
            for (int y = 0; y < kernelSize; y++) {
                double distance = Math.sqrt((x - kernelSize / 2) * (x - kernelSize / 2) + (y - kernelSize / 2) * (y - kernelSize / 2));
                kernel[x][y] = Math.exp(-distance * distance / (2 * sigma * sigma)) / (2 * Math.PI * sigma * sigma);
                sum += kernel[x][y];
            }
        }
        for (int x = 0; x < kernelSize; x++) {
            for (int y = 0; y < kernelSize; y++) {
                kernel[x][y] /= sum;
            }
        }
        return kernel;
    }
    private BufferedImage getImage(int[][] pixels, int type) {
        int width = pixels.length;
        int height = pixels[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, pixels[x][y]);
            }
        }
        return image;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    private int[][] getEdges(int[][] pixels) {
        int width = pixels.length;
        int height = pixels[0].length;
        int BLACK = 0;
        int WHITE = 0xffffff;
        int THRESHOLD = 20;
        double[][] gradientX = new double[width][height];
        double[][] gradientY = new double[width][height];
        double[][] gradient = new double[width][height];
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                gradientX[x][y] = pixels[x + 1][y] - pixels[x - 1][y];
                gradientY[x][y] = pixels[x][y + 1] - pixels[x][y - 1];
                gradient[x][y] = Math.sqrt(gradientX[x][y] * gradientX[x][y] + gradientY[x][y] * gradientY[x][y]);
            }
        }
        int[][] edges = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                edges[x][y] = gradient[x][y] > THRESHOLD ? BLACK : WHITE;
            }
        }
        return edges;
    }

    public static BufferedImage concatenateImages(BufferedImage[] images) {
        int width = images[0].getWidth();
        int height = 0;
        for (BufferedImage image : images) {
            height += image.getHeight();
        }
        int type = images[0].getType();
        BufferedImage concatenatedImage = new BufferedImage(width, height, type);

        int y = 0;
        for (BufferedImage image : images) {
            concatenatedImage.createGraphics().drawImage(image, 0, y, null);
            y += image.getHeight();
        }

        return concatenatedImage;
    }
}
