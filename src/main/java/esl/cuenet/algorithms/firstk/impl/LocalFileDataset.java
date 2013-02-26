package esl.cuenet.algorithms.firstk.impl;

import esl.cuenet.algorithms.firstk.Dataset;

import java.io.File;

public class LocalFileDataset implements Dataset<File> {

    private File file;
    private String[] annotations;

    public LocalFileDataset(File file, String[] annotations) {
        this.file = file;
        this.annotations = annotations;
    }

    public LocalFileDataset(String filename) {
        file = new File(filename);
    }

    @Override
    public File item() {
        return file;
    }

    public String[] getAnnotations() {
        return annotations;
    }
}

