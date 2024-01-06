import java.io.FileInputStream;
import java.util.*;


public class MyCompression {
    private int numVectors;
    private int vectorSize;
    private byte[] bytes;
    private int[] rgbIntArr;

    private MyCompression(String file, int M, int N) {
        numVectors = M;
        vectorSize = N;
        String filename = file;
        try (FileInputStream fis = new FileInputStream(filename)) {
            bytes = fis.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        rgbIntArr = new int[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            rgbIntArr[i] = (int) bytes[i] + 127;
        }
        
    }   
    public double Euclidean (double x1, double y1, double x2, double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    private ArrayList<int[]> kMeansClustering (ArrayList<int[]> vectors, int N, int M) {
        ArrayList<int[]> centroids = new ArrayList<int[]>();
        Random random = new Random();
        // Randomly choose the initial centroids
        for (int i = 0; i < N; i++) {
            int nextX = random.nextInt(255 - 1 - 0) + 0;
            int nextY = random.nextInt(255 - 1 - 0) + 0;
            int[] newCentroid = new int[M];
            newCentroid[0] = nextX;
            newCentroid[1] = nextY;
            centroids.add(newCentroid);
        }
        boolean loop = true;
        while (loop) {
            ArrayList<int[]> prevCentroids = new ArrayList<int[]>();
            for (int i = 0; i < N; i++) {
                int[] copy = new int[M];
                copy[0] = centroids.get(i)[0];
                copy[1] = centroids.get(i)[1];
                prevCentroids.add(copy);
            }
            // Initialize list of clusters
            ArrayList<ArrayList<Integer>> clusterIndices = new ArrayList<ArrayList<Integer>>();
            for (int i = 0; i < N; i++) { 
                ArrayList<Integer> clusters = new ArrayList<Integer>();
                clusterIndices.add(clusters);
            }
            for (int i = 0; i < vectors.size(); i++) {
                double minDist = Double.MAX_VALUE;
                int currCentroidAssignment = -1;    
                for (int j = 0; j < centroids.size(); j++) { 
                    double euclid = Euclidean(vectors.get(i)[0], vectors.get(i)[1], centroids.get(j)[0], centroids.get(j)[1]);
                    if (euclid < minDist) {
                        minDist = euclid;
                        currCentroidAssignment = j;
                    }
                }
                clusterIndices.get(currCentroidAssignment).add(i);     
            }
            // Recalculate the centroids: For each cluster, 
            // calculate the mean of all the data points assigned to that cluster, and set that as the new centroid.
            for (int i = 0; i < N; i++) {
                int[] newCentroid = new int[M];
                if (clusterIndices.get(i).size() == 0) { // Randomize X, Y value if codeword has no nearest neighbors
                    newCentroid[0] = random.nextInt(255 - 1 - 0) + 0;
                    newCentroid[1] = random.nextInt(255 - 1 - 0) + 0;
                    centroids.set(i, newCentroid);
                }
                else {
                    int running_total_x = 0;
                    int running_total_y = 0;
                    for (int j = 0; j < clusterIndices.get(i).size(); j++) {
                        running_total_x += vectors.get(clusterIndices.get(i).get(j))[0];
                        running_total_y += vectors.get(clusterIndices.get(i).get(j))[1];
                    }
                    int avg_x = (int) (running_total_x / clusterIndices.get(i).size());
                    int avg_y = (int) (running_total_y / clusterIndices.get(i).size());
                    newCentroid[0] = avg_x;
                    newCentroid[1] = avg_y;
                    centroids.set(i, newCentroid);
                }
            }
            // If there is no change, loop ends
            if (centroids.equals(prevCentroids)) {
                loop = false;
            }
            // If no change greater than 2 is detected, loop ends
            boolean change = false;
            for (int i = 0; i < N; i++) {
                double diff_x = Math.abs(centroids.get(i)[0] - prevCentroids.get(i)[0]);
                double diff_y = Math.abs(centroids.get(i)[1] - prevCentroids.get(i)[1]);
                if (diff_x > 2 || diff_y > 2) {
                    change = true;
                }
            }
            if (!change) {
                loop = false;
            }
        }
        return centroids;
    }

    public static void main(String[] args) {
        String param0 = args[0]; // file
        int param1 = Integer.parseInt(args[1]); // M
        int param2 = Integer.parseInt(args[2]); // N
        System.out.println("The first parameter was: " + param0);
	    System.out.println("The second parameter (M) was: " + param1);
	    System.out.println("The third parameter (N) was: " + param2);
        
        MyCompression compression = new MyCompression(param0, param1, param2);
        ImageDisplay ren = new ImageDisplay(352, 288, compression.bytes, param1);
        ArrayList<int[]> initCodebook = ren.getInitialCodebook();
        ArrayList<int[]> codebookVectors = compression.kMeansClustering(initCodebook, param2, param1);
        ren.setFinalCodebook(codebookVectors);
        ren.showIms(352, 288, compression.bytes, param1);

    }
}   