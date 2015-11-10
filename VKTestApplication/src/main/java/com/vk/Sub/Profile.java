package com.vk.Sub;

/**
 * Created by Артем on 07.11.2015.
 */
public class Profile {

    public String name;
    public String image;
    public boolean online;


    public Profile(String _describe, String _image, boolean _online) {
        name = _describe;
        image = _image;
        online = _online;
    }
}