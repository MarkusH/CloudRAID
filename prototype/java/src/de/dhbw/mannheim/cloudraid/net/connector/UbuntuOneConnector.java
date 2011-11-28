package de.dhbw.mannheim.cloudraid.net.connector;

import java.io.InputStream;

public class UbuntuOneConnector implements IStorageConnector {
    
    

    @Override
    public boolean connect(String service) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean put(String resource) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public InputStream get(String resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean delete(String resource) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String post(String resource, String parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] options(String resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String head(String resource) {
        // TODO Auto-generated method stub
        return null;
    }

}
