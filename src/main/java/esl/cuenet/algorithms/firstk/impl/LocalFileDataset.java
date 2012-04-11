package esl.cuenet.algorithms.firstk.impl;

import esl.cuenet.algorithms.firstk.Dataset;

import java.io.File;

public class LocalFileDataset implements Dataset<File> {

    private File file;

    public LocalFileDataset(File file) {
        this.file = file;
    }

    public LocalFileDataset(String filename) {
        file = new File(filename);
    }

    @Override
    public File item() {
        return file;
    }
}

