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

package de.audiogrid.devices.rs232.service;

import de.audiogrid.devices.rs232.framework.IntersektCommunicationException;
import de.audiogrid.devices.rs232.framework.SerialPortConfiguration;
import de.audiogrid.devices.rs232.framework.SerialPortInformation;

/**
 * This class encapsulates the config for a serial attached LINN RS232 device. It also checks if
 * a device is attached to a certain serial Port.
 */
public class RS232Configuration extends SerialPortConfiguration
{
    public RS232Configuration()
    {
    }

    public RS232Configuration(SerialPortInformation information)
    {
        this.information = information;
    }

    public void setSerialPortInformation(SerialPortInformation information)
    {
        this.information = information;
        this.setPortId(information.getPort());
    }


    /**
     * checks whether a Linn RS232 device instance is connected to that port. Depending on the installation
     * an empty String is given back.
     * @return
     */
    public SerialPortInformation call()
    {

        try
        {
            openConnection();
            String result = sendData("$ID ?$\r\n");
            log.info(result);
            if(result != null)
            {
                log.info("found LINN RS232 device, ID is " + result);
                information.setType(SerialPortInformation.LINN_MULTIROOM);
            }
            return information;
        }
        catch(Exception e)
        {
            log.error("unable to communicate with serial port " + portId.getName(), e);
            log.error("Linn RS232 device not found on: " + portId.getName());
        }
        finally
        {
            closeConnection();
        }
        information.setType(SerialPortInformation.NODEVICE);
        return information;
    }

    @Override
    protected void populateConnectionParameters() throws IntersektCommunicationException
    {
        try
        {
            setConnectionParameters(this);
        }
        catch (IntersektCommunicationException e)
        {
            sPort.close();
            throw e;
        }
    }

    @Override
    public void initializeDevice()
    {
        log.info("initialize device with $ID $");
        log.info(sendData("$ID ?$\r\n"));
    }
}
