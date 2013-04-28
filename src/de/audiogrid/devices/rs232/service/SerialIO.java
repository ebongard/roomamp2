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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.audiogrid.devices.rs232.framework.BaseSerialIO;
import de.audiogrid.devices.rs232.framework.NoPortConnectedException;
import de.audiogrid.devices.rs232.framework.SerialPortDetector;
import de.audiogrid.devices.rs232.model.RS232Device;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SerialIO extends BaseSerialIO
{

    private int delay = 750;
    Log log = LogFactory.getLog(getClass());

    HashMap<String,RS232Device> model = new HashMap<String,RS232Device>();

    static SerialIO instance;

        static
        {
            instance = new SerialIO();
        }

        public static SerialIO getInstance()
        {
            return instance;
        }

        public static void destroyInstance()
        {
            instance.closeConnection();
            instance = null;
        }

    private SerialIO()
        {
            SerialPortDetector detector = new SerialPortDetector();
            try
            {
                setPortId(detector.checkForDevice(new RS232Configuration(null)));
                if(!isOpen())
                    openConnection();
            }
            catch(NoPortConnectedException ex)
            {
                log.debug("No device found");
            }
            catch (Exception e)
            {
                log.error("unable to open connection",e);
            }
        }

    public int getDelay()
    {
        return delay;
    }

    public void setDelay(int delay)
    {
        this.delay = delay;
    }

    public ArrayList<RS232Device> getDevices()
    {
        ArrayList<RS232Device> devices = new ArrayList<RS232Device>();
        for (String s : getIDs())
        {
            RS232Device d = new RS232Device();
            d.setIdentifier(s);
            d.setDaisyChain(false);
            d.setPath("@" + s + "@");
            devices.add(d);
            log.info("adding device " + d + " to model");

            model.put(s,d);
        }
        for (String s : model.keySet())
        {
            RS232Device d = this.populateDevice(s);
            log.info(d);
            model.put(s,d);
        }
        return devices;
    }

    public RS232Device getDeviceByID(String deviceIdentifier)
    {
        return model.get(deviceIdentifier);
    }

    /**
     * @todo need to parse response correctly and return correct status
     */
    public int issueCommand(String deviceIdentifier, String command,String value,boolean iseql)
    {
        String data;
        RS232Device device = model.get(deviceIdentifier);
        String response = sendData("$STANDBY OFF$\r\n");
        log.info(response);
        try{

            Thread.sleep(delay);
        }catch(InterruptedException ex){
            //do stuff
        }
        String eql;
        if(iseql)
            eql = " = ";
        else
            eql = " ";

        if(device.isDaisyChain())
            data = device.getPath() + "$"+command + eql + value + "$\r\n";
        else
            data = "$" + command + eql + value + "$\r\n";

            response = sendData(data);

        HashMap<String,String> formattedResult = responseParser(response);
        setDeviceData(device, formattedResult);
        model.put(deviceIdentifier,device);

        return 0;
    }

    public RS232Device queryDevice(String deviceIdentifier,String command)
    {
        String data,response;
        RS232Device device = model.get(deviceIdentifier);


        if(device.isDaisyChain())
            data = device.getPath() + "$"+command + " ?$\r\n";
        else
            data = "$" + command + " ?$\r\n";

        response = sendData(data);
        HashMap<String,String> formattedData = responseParser(response);
        setDeviceData(device, formattedData);
        model.put(deviceIdentifier,device);

        return device;

    }

    public ArrayList <String> getIDs()
    {
        ArrayList<String> ids = new ArrayList<String>();
        String response = sendData("$STANDBY OFF$\r\n");
        try{

            Thread.sleep(delay);
        }catch(InterruptedException ex){
            //do stuff
        }

        String result = sendData("$ID ?$\r\n");
        ids.add(trimIDs(result)) ;
        return ids;
    }

    public RS232Device populateDevice(String deviceIdentifier)
    {
        log.info("trying to find device for id " + deviceIdentifier + " in " + model);
        RS232Device device = model.get(deviceIdentifier);

        String result="";
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("$VERSION SOFTWARE ?$\r\n");
        commands.add("$VERSION HARDWARE ?$\r\n");
        commands.add("$BAL ?$\r\n");
        commands.add("$VOL ?$\r\n");
        commands.add("$BAS ?$\r\n");
        commands.add("$TRB ?$\r\n");
        commands.add("$MUTE ?$\r\n");
        commands.add("$LISTEN ?$\r\n");
        commands.add("$STANDBY ?$\r\n");
        commands.add("$COUNTER POWER$\r\n");
        commands.add("$COUNTER MAINS$\r\n");
        commands.add("$STATUS$\r\n");


        for(String command:commands)
        {
            try{

                Thread.sleep(delay);
            }catch(InterruptedException ex){
                //do stuff
            }
            if(device.isDaisyChain())
                result = sendData(device.getPath() + command);
            else
                result = sendData(command);

            HashMap<String,String> formattedResult = responseParser(result);

            setDeviceData(device, formattedResult);
        }

        return device;
    }

    private void setDeviceData(RS232Device device, HashMap<String,String> formattedResult)
    {
        for (Map.Entry<String, String> entry : formattedResult.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            device.addContent(key,value);
        }
    }

    private String trimIDs(String id)
    {
        id = id.replaceAll("\n\n","");
        id = id.replace('!', ' ');
        id = id.replace('$', ' ');
        id = id.replaceAll("ID ", "");
        id = id.trim();
        log.info(id);
        return id;
    }

    private HashMap<String,String> responseParser(String response)
    {
        ArrayList<String> content = new ArrayList<String>();
        HashMap<String,String> target = new HashMap<String, String>();

        String a = response.replace("!\n","");

        String[] b = a.split("\n");
        for(String c:b)
        {
            if(c!=null && c.trim().length()>1)
            {

                c = c.replace("!","");
                c = c.replace("$","");
                content.add(c);

            }
        }

        for (String line:content)
        {
            String[] d = line.split(" ");

            if(d.length == 3) //e.g.   COUNTER MAINS 0:897:23:17
            {
                target.put(d[0]+" "+d[1],d[2]);
            }
            else if (d.length==2) // e.g. BAL -2
            {
                target.put(d[0], d[1]);
            }
            else
            {
                log.warn("cannot process line " + line);
            }
        }
        return target;
    }



    protected void finalize()
        {
            try
            {
                super.finalize();
            }
            catch (Throwable throwable)
            {
                log.error(throwable);
            }
            closeConnection();
        }
    }

