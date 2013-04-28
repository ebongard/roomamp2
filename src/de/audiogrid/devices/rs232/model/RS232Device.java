/*
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *       THIS MATERIAL IS PROPRIETARY  TO  EDUARD VAN DEN BONGARD
 *       AND IS  NOT TO BE REPRODUCED, USED OR  DISCLOSED  EXCEPT
 *       IN ACCORDANCE  WITH  PROGRAM  LICENSE  OR  UPON  WRITTEN
 *       AUTHORIZATION  OF  EDUARD VAN DEN BONGARD
 *
 *       COPYRIGHT (C) 2013 EDUARD VAN DEN BONGARD
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package de.audiogrid.devices.rs232.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

@XmlRootElement (name="RS232Device")
public class RS232Device  implements Comparable
{
    String name;
    String identifier;
    String groupid;
    int status;
    String link;
    String description;
    String image;
    String versionSW;
    String versionHW;
    String power;
    String mains;
    String path;
    boolean isDaisyChain;
    HashMap<String,String> content = new HashMap<String, String>();

    public RS232Device()
    {
        this("UNKNOWN");
    }

    public RS232Device(String name)
    {
        this.name = name;
    }

    public String getName(){ return name; }

    public void setName(String name){ this.name = name; }

    @Override
    public String toString()
    {
        return "RS232Device{" +
                "identifier='" + identifier + '\'' +
                ", groupid='" + groupid + '\'' +
                ", status=" + status +
                ", link='" + link + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", versionSW='" + versionSW + '\'' +
                ", versionHW='" + versionHW + '\'' +
                ", power='" + power + '\'' +
                ", mains='" + mains + '\'' +
                ", path='" + path + '\'' +
                ", isDaisyChain=" + isDaisyChain +
                ", content=" + content +
                ", name='" + name + '\'' +
                '}';
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }


    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getGroupid()
    {
        return groupid;
    }

    public void setGroupid(String groupid)
    {
        this.groupid = groupid;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public String getVersionSW()
    {

        return content.get("VERSION SOFTWARE");
    }

    public void setVersionSW(String versionSW)
    {
        this.versionSW = versionSW;
    }

    public String getVersionHW()
    {

        return content.get("VERSION HARDWARE");
    }

    public void setVersionHW(String versionHW)
    {
        this.versionHW = versionHW;
    }

    public String getPower()
    {

        return content.get("COUNTER POWER");
    }

    public void setPower(String power)
    {
        this.power = power;
    }

    public String getMains()
    {

        return content.get("COUNTER MAINS");
    }

    public void setMains(String mains)
    {
        this.mains = mains;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public boolean isDaisyChain()
    {
        return isDaisyChain;
    }

    public void setDaisyChain(boolean daisyChain)
    {
        isDaisyChain = daisyChain;
    }

    public HashMap<String, String> getContent()
    {
        return content;
    }

    public void setContent(HashMap<String, String> content)
    {
        this.content = content;
    }

    public void addContent(String key, String value)
    {
        content.put(key,value);
    }

    public String getBas()
    {

        return content.get("BAS");
    }

    public String getTreb()
    {

        return content.get("TRB");
    }

    public String getMute()
    {

        return content.get("MUTE");
    }

    public String getVolume()
    {

        return content.get("VOL");
    }

    public String getBalance()
    {

        return content.get("BAL");
    }

    public String getListen()
    {

        return content.get("LISTEN");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof RS232Device)) return false;

        RS232Device that = (RS232Device) o;

        if (status != that.status) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (groupid != null ? !groupid.equals(that.groupid) : that.groupid != null) return false;
        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;
        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        if (link != null ? !link.equals(that.link) : that.link != null) return false;
        if (mains != null ? !mains.equals(that.mains) : that.mains != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (power != null ? !power.equals(that.power) : that.power != null) return false;
        if (versionHW != null ? !versionHW.equals(that.versionHW) : that.versionHW != null) return false;
        if (versionSW != null ? !versionSW.equals(that.versionSW) : that.versionSW != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (groupid != null ? groupid.hashCode() : 0);
        result = 31 * result + status;
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        result = 31 * result + (versionSW != null ? versionSW.hashCode() : 0);
        result = 31 * result + (versionHW != null ? versionHW.hashCode() : 0);
        result = 31 * result + (power != null ? power.hashCode() : 0);
        result = 31 * result + (mains != null ? mains.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public int compareTo(Object o)
    {
        if(getClass().getName().equalsIgnoreCase(o.getClass().getName()))
               return 0;
           else
               return -1;

    }

}