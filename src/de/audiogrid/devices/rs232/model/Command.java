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

@XmlRootElement(name="Command")
public class Command
{
    String deviceIdentifier;
    String key;
    String value;
    String zone;
    boolean iseql;

    public Command()
    {
    }

    public Command(String deviceIdentifier, String key, String value, String zone,boolean iseql)
    {
        this.deviceIdentifier = deviceIdentifier;
        this.key = key;
        this.value = value;
        this.zone = zone;
        this.iseql = iseql;
    }

    public String getDeviceIdentifier()
    {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier)
    {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getZone()
    {
        return zone;
    }

    public void setZone(String zone)
    {
        this.zone = zone;
    }

    public boolean isIseql()
    {
        return iseql;
    }

    public void setIseql(boolean iseql)
    {
        this.iseql = iseql;
    }
}
