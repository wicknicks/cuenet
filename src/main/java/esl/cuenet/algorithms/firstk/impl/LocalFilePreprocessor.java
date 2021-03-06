package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.algorithms.firstk.exceptions.CorruptDatasetException;
import esl.cuenet.algorithms.firstk.Dataset;
import esl.cuenet.algorithms.firstk.Preprocessing;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Event;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.model.Constants;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LocalFilePreprocessor implements Preprocessing<File> {

    private Logger logger = Logger.getLogger(ExifExtractor.class.getName());
    private ExifExtractor extractor = new ExifExtractor();
    private EventGraph graph = null;
    private OntModel model = null;

//    private String username = "Setareh Rad";
//    private String email = "setareh.rafatirad@gmail.com";
    private String username = "Ramesh Jain";
    private String email = "jain49@gmail.com";
//    private String username = "Arjun Satish";
//    private String email = "arjun.satish@gmail.com";

    public LocalFilePreprocessor(OntModel model, String username, String email) {
        this.model = model;
        this.username = username;
        this.email = email;
    }

    public LocalFilePreprocessor(OntModel model) {
        this.model = model;
    }

    @Override
    public EventGraph process(Dataset<File> fileDataset) throws CorruptDatasetException {
        if (fileDataset == null) throw new CorruptDatasetException("Dataset = NULL");
        File file = fileDataset.item();
        SimpleDateFormat sdformatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

        if (file == null) throw new CorruptDatasetException("Dataset content = NULL");
        if (!file.exists()) throw new CorruptDatasetException("File not found: " + file.getAbsolutePath());

        graph = new EventGraph(model);
        try {
            Exif exif = extractor.extractExif(file.getAbsolutePath());
            logger.info("Processing: " + file.getAbsolutePath());


            logger.info("Timestamp: " + exif.timestamp);
            logger.info("Formatted Time: " + sdformatter.format(new Date(exif.timestamp)));
            //change to observe ranker for any photo (crude hack to disable event context).
            //exif.timestamp = 0L;

            logger.info("GPS-Lat: " + exif.GPSLatitude);
            logger.info("GPS-Lon: " + exif.GPSLongitude);
            logger.info("Image-Width: " + exif.width);
            logger.info("Image-Height: " + exif.height);
            logger.info("User name: " + username);
            logger.info("Email: " + email);

            Event io;
            io = graph.createEvent(Constants.PhotoCaptureEvent);
            io.addLiteral(model.createProperty(
                    model.getNsPrefixMap().get(Constants.DefaultNamespace) + Constants.ImageWidth),
                    model.createTypedLiteral(exif.width));
            io.addLiteral(model.createProperty(
                    model.getNsPrefixMap().get(Constants.DefaultNamespace) + Constants.ImageHeight),
                    model.createTypedLiteral(exif.height));
            io.addResource(model.getProperty(Constants.CuenetNamespace + Constants.OccursDuring),
                    TimeInterval.createFromMoment(exif.timestamp, model));
            io.getIndividual().addProperty(model.getProperty(Constants.CuenetNamespace + "title"),
                    "photo-capture-event");

            Entity author = null;
            if (username != null || email != null) {
                author = graph.createPerson();
            }

            if (username != null)
                author.addLiteral(model.createProperty(
                    model.getNsPrefixMap().get(Constants.DefaultNamespace) + Constants.Name),
                    model.createTypedLiteral(username));

            if (email != null)
                author.addLiteral(model.createProperty(
                    model.getNsPrefixMap().get(Constants.DefaultNamespace) + Constants.Email),
                    model.createTypedLiteral(email));

            if (exif.GPSLatitude != 0 && exif.GPSLongitude != 0) {
                io.addResource(model.getProperty(Constants.CuenetNamespace + Constants.OccursAt),
                        Location.createFromGPS(exif.GPSLatitude, exif.GPSLongitude, model));
            }

            graph.addParticipant(io, author);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (EventGraphException e) {
            e.printStackTrace();
        }

        return graph;
    }

    @Override
    public void associate(Individual individual, EventGraph.NodeType type) {
        if (graph == null) throw new RuntimeException("individual cannot be assigned to NULL graph");
        graph.addIndividual(individual, type);
    }

    public static class Exif {
        public Integer width = null;
        public Integer height = null;
        public Long timestamp = null;
        public String mimetype = null;
        public Double GPSLatitude = null;
        public Double GPSLongitude = null;

        public String toString() {
            return width + "x" + height + " ; " + timestamp + " @ (" + GPSLatitude + ", " + GPSLongitude + ")";
        }
    }

    public static class ExifExtractor {

        private String exiftoolPath = "exiftool --help";
        private String args[] = {"-d \"%s\" -datetimeoriginal", "-c \"%.6f\"",
                "-gpslatitude", "-gpslongitude", "-imagewidth", "-imageheight", "-mimetype"};
        private String command = null;
        private Exif exif = null;
        private Logger logger = Logger.getLogger(ExifExtractor.class);

        private HashMap<String, Exif> cache  = new HashMap<String, Exif>();
        private String cacheFile = "/data/test_photos/tina/highres/exif.cache";

        public ExifExtractor() {
            StringBuilder builder = new StringBuilder();
            builder.append(exiftoolPath);
            builder.append(' ');
            for (String arg : args) {
                builder.append(arg);
                builder.append(' ');
            }
            command = builder.substring(0);

            File file = new File(cacheFile);
            if ( ! file.exists() ) return;

            try {
                for (String line: FileUtils.readLines(file)) {
                    String[] parts = line.split(" ");
                    Exif e = new Exif();
                    e.timestamp = Long.parseLong(parts[1]);
                    e.GPSLatitude = Double.parseDouble(parts[2]);
                    e.GPSLongitude = Double.parseDouble(parts[3]);
                    cache.put(parts[0], e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

            if (cache.containsKey(exifImage)) return cache.get(exifImage);

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

                //logger.info(command + " " + exifImage);

                proc = Runtime.getRuntime().exec(command + " " + exifImage);
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
