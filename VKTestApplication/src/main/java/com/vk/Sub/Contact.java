package com.vk.Sub;

import android.net.Uri;

/**
 * Created by Артем on 06.12.2015.
 */
public class Contact extends Profile {
    public long contactId;
    public Uri contactUri;
    public String displayName;
    public String photoId;

    public boolean online;
    public boolean monline;

    public Contact() {
        super(null, null, false);
    }

    public Contact(String _describe, String _image, boolean _online, boolean _monline) {
        super(_describe, _image, _online);
        displayName = _describe;
        online = _online;
        monline = _monline;
        if (_monline) {
            online = _monline;
        }
        //  photoId = _image;
        //contactUri = new Uri
    }

    public long getContactId() {
        return contactId;
    }

    public Uri getContactUri() {
        return contactUri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhotoId() {
        return photoId;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isMonline() {
        return monline;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public void setContactUri(Uri contactUri) {
        this.contactUri = contactUri;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setMonline(boolean monline) {
        this.monline = monline;
    }

    @Override
    public String toString() {
        return "Contact{" + "Name=" + name + '}';
    }
}
