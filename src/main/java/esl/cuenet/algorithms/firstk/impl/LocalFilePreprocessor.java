package esl.cuenet.algorithms.firstk.impl;

import esl.cuenet.algorithms.firstk.*;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Event;
import esl.datastructures.graph.relationgraph.RelationGraph;
import org.apache.log4j.Logger;

import java.io.*;

public class LocalFilePreprocessor implements Preprocessing<File> {

    private Logger logger = Logger.getLogger(ExifExtractor.class.getName());
    private ExifExtractor extractor = new ExifExtractor();
    private RelationGraph graph = new RelationGraph();


    @Override
    public RelationGraph process(Dataset<File> fileDataset) throws CorruptDatasetException {

        File file = fileDataset.item();

        if (file == null) throw new CorruptDatasetException("Dataset content = NULL");
        if (!file.exists()) throw new CorruptDatasetException("File not found: " + file.getAbsolutePath());

        try {
            Exif exif = extractor.extractExif(file.getAbsolutePath());
            logger.info("Processing: " + file.getAbsolutePath());
            logger.info("Timestamp: " + exif.timestamp);
            logger.info("GPS-Lat: " + exif.GPSLatitude);
            logger.info("GPS-Lon: " + exif.GPSLongitude);
            logger.info("Image-Width: " + exif.width);
            logger.info("Image-Height: " + exif.height);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void associate(Event event) {
        graph.createNode(event.name());
    }

    @Override
    public void associate(Entity entity) {
        graph.createNode(entity.name());
    }

    class Exif {
        Integer width = null;
        Integer height = null;
        Long timestamp = null;
        String mimetype = null;
        Double GPSLatitude = null;
        Double GPSLongitude = null;
    }

    class ExifExtractor {

        private String exiftoolPath = "exiftool --help";
        private String args[] = {"-d \"%s\" -datetimeoriginal", "-c \"%.6f\"",
                "-gpslatitude", "-gpslongitude", "-imagewidth", "-imageheight", "-mimetype"};
        private String command = null;
        private Exif exif = null;

        public ExifExtractor() {
            StringBuilder builder = new StringBuilder();
            builder.append(exiftoolPath);
            builder.append(' ');
            for (String arg : args) {
                builder.append(arg);
                builder.append(' ');
            }
            command = builder.substring(0);
        }

        public int getWidth() {
            return exif.width;
        }

        public int getHeight() {
            return exif.height;
        }

        public Double getLatitude() {
            return exif.GPSLatitude;
        }

        public Double getLongitude() {
            return exif.GPSLongitude;
        }

        public Long getTimestamp() {
            return exif.timestamp;
        }


        public Exif extractExif(String exifImage) throws IOException {

            StringBuilder exifBuilder = new StringBuilder();
            exif = null;

            if (exifImage == null) {
                throw new IllegalArgumentException("exifImage is null");
            }

            if (exifImage.contains(" ")) {
                throw new IllegalArgumentException("Image cannot contain a whitespace");
            }

            Process proc = null;
            DataInputStream exifStream = null;

            try {
                exif = new Exif();

                proc = Runtime.getRuntime().exec(command + " " + exifImage + "");
                exifStream = new DataInputStream(proc.getInputStream());

                while (true) {
                    char c = (char) exifStream.readByte();
                    if (c != '"')
                        exifBuilder.append(c);
                    if (c == '\n') {
                        parse(exifBuilder);
                        exifBuilder = null;
                        exifBuilder = new StringBuilder();
                    }
                }

            } catch (EOFException ex) {
                /* datainputstream reached an EOF */
            }

            try {
                if (proc != null) proc.waitFor();
                if (exifStream != null) exifStream.close();
                if (proc != null) proc.destroy();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return exif;
        }

        private void parse(StringBuilder input) {
            int ix = input.indexOf(":");
            if (ix == -1) {
                logger.info("Returning.. " + input);
                return;
            }

            String sequence = input.substring(0, ix).trim();
            if (sequence.compareTo("Date/Time Original") == 0) {
                exif.timestamp = 1000 * Long.parseLong(input.substring(ix + 1).trim());
            } else if (sequence.compareTo("GPS Latitude") == 0) {
                exif.GPSLatitude = parseCoordinate(input.substring(ix + 1).trim());
            } else if (sequence.compareTo("GPS Longitude") == 0) {
                exif.GPSLongitude = parseCoordinate(input.substring(ix + 1).trim());
            } else if (sequence.compareTo("Image Width") == 0) {
                exif.width = Integer.parseInt(input.substring(ix + 1).trim());
            } else if (sequence.compareTo("Image Height") == 0) {
                exif.height = Integer.parseInt(input.substring(ix + 1).trim());
            } else if (sequence.compareTo("MIME Type") == 0) {
                exif.mimetype = input.substring(ix + 1).trim();
            }
        }

        private Double parseCoordinate(String a) {
            Double decimal = 0.0;
            int ix = a.indexOf(' ');

            if (ix == -1) {
                decimal = Double.parseDouble(a);
                return decimal;
            }

            decimal = Double.parseDouble(a.substring(0, ix));

            String s = a.substring(ix + 1);
            if (s.compareTo("W") == 0 || s.compareTo("S") == 0) decimal = -decimal;

            return decimal;
        }
    }


}
