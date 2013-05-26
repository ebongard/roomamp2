package de.audiogrid.devices.rs232.service;/*
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
    ArrayList<String> ids = new ArrayList<String>();

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

    public void prepare()
    {
        sendData("$STANDBY OFF$\r\n");
    }

    public ArrayList <RS232Device> getIDs()
    {
        prepare();

        try{

            Thread.sleep(delay);
        }catch(InterruptedException ex){
            //do stuff
        }

        String deviceIdentifier = sendData("$ID ?$\r\n");

        // check if an ID is pollable
        if(deviceIdentifier.contains("KINOS") || deviceIdentifier.contains("UNIDISK"))
        {
            log.info("found id that can be polled, " + deviceIdentifier);
            poll(null);
        }
        else if (deviceIdentifier.contains("LR2"))
        {
            // there can only on RoomAmp2 per serial connection, so we reduce it to LR2
            log.info("found id that can be NOT polled, " + deviceIdentifier);
            //String processedDeviceIdentifier = trimIDs(deviceIdentifier);
            ids.add("LR2") ;
            RS232Device d = createDevice("LR2",false);
            model.put("LR2",d);

        }
        else if (deviceIdentifier.contains("IRT"))
        {
            // some Roomamps do have 2 IDs, there can only on RoomAmp2 per serial connection, so we reduce it to LR2
            log.info("found id that can be NOT polled, " + deviceIdentifier);
            //String processedDeviceIdentifier = trimIDs(deviceIdentifier);
            ids.add("LR2") ;
            RS232Device d = createDevice("LR2",false);
            model.put("LR2",d);

        }
        else
        {
            log.info("found and unknown id, " + deviceIdentifier);
            String processedDeviceIdentifier = trimIDs(deviceIdentifier);
            ids.add(processedDeviceIdentifier) ;
            RS232Device d = createDevice(processedDeviceIdentifier,false);
            model.put(processedDeviceIdentifier, d);
        }

        // now we need to check if we have UNIDISK SC's connected. It's not possible to do that during polling,
        // as additional commands are not allowed.
        if(model.keySet().contains("UNIDISK"))
        {
            log.info("found an UNIDISK in the chain");
            RS232Device tmpDevice = model.get("UNIDISK");
            //assuming that there is only one UNIDISK in the chain
            if(isUNIDISKSC())
            {
                log.info("UNIDISK device is an UNIDISKSC, removing old one, adding new one");
                tmpDevice.setIdentifier("UNIDISKSC");
                model.remove("UNIDISK");

                model.put("UNIDISKSC",tmpDevice);
            }
        }
        // populate devices first
        getDevices();

        return new ArrayList<RS232Device>(model.values());
        }

    public void poll(String currentDevice)
    {
        if(currentDevice == null)
        {
            log.info(sendData("$POLL START$\r\n"));
            String deviceIdentifier = sendData("$POLL ID$\r\n");
            deviceIdentifier = trimIDs(deviceIdentifier);
            log.info(deviceIdentifier);
            if (deviceIdentifier == null || deviceIdentifier.length()==0)
                deviceIdentifier = "NO_MORE_POLLING";
            else
            {
                log.info("adding device to ids");
                ids.add(deviceIdentifier);
                RS232Device d = createDevice(deviceIdentifier,true);
                model.put(deviceIdentifier,d);
            }

            poll(deviceIdentifier);
        }
        else if(currentDevice.equalsIgnoreCase("NO_MORE_POLLING"))
        {
            log.info(sendData("$POLL DONE$\r\n"));
        }
        else
        {
            log.info(sendData("@" + currentDevice +"@$POLL SLEEP$\r\n"));
            String deviceIdentifier = sendData("$POLL ID$\r\n");
            deviceIdentifier = trimIDs(deviceIdentifier);
            log.info(deviceIdentifier);
            // if result is empty, then there is no device left to be polled
            if (deviceIdentifier == null || deviceIdentifier.length()==0)
                deviceIdentifier = "NO_MORE_POLLING";
            else
            {
                log.info("adding device to ids");
                ids.add(deviceIdentifier);
                RS232Device d = createDevice(deviceIdentifier,true);
                model.put(deviceIdentifier,d);
            }

            poll(deviceIdentifier);
        }
    }

    private boolean isUNIDISKSC()
    {
        // only UNIDISKSC responds with data, UNDISK will fail
        String result = sendData("@UNIDISK@$VOL ?$\r\n");
        log.info("isUNIDISKSC:" + result);
        if(result.contains("FAIL"))
            return false;
        else
            return true;
    }


    private  RS232Device createDevice(String deviceIdentifier,boolean isPollable)
    {
        log.info("creating RS232Device for id " + deviceIdentifier);
        RS232Device d;

        d = new RS232Device();

        d.setIdentifier(deviceIdentifier);

        if(isPollable)
        {
            d.setDaisyChain(true);
            //need to set path to UNIDISK
            if(deviceIdentifier.contains("UNIDISK"))
                d.setPath("@UNIDISK@");
            else
                d.setPath("@" + deviceIdentifier + "@");
        }
        else
        {
            d.setDaisyChain(false);
            d.setPath("");
        }
        return d;
    }

    public ArrayList<RS232Device> getDevices()
    {
        //getIDs();

        for (String s : model.keySet())
        {
            RS232Device d = populateDevice(s);
            log.info(d);
            model.put(s,d);
        }
        return new ArrayList<RS232Device>(model.values());
    }

    public RS232Device getDeviceByID(String deviceIdentifier)
    {
        log.info("get device for " + deviceIdentifier + " is " + model.get(deviceIdentifier));
        // the device is not yet filled here, so we need to query the device first.
        return model.get(deviceIdentifier);
    }

    /**
     * @todo need to parse response correctly and return correct status
     */
    public int issueCommand(String deviceIdentifier, String command,String value,String zone,boolean iseql)
    {
        String data;
        RS232Device device = model.get(deviceIdentifier);
        String response = sendData("$STANDBY OFF$\r\n");
        log.info(response);
        // setting zone
        // This is only needed for roomamp 2
        if(zone != null && zone.trim().length()>0)
        {
            response = sendData("$ORIGIN " + zone + "$\r\n");
            log.info(response);
            try{

                Thread.sleep(delay);
            }catch(InterruptedException ex){
                //do stuff
            }
        }

        String eql;
        if(iseql)
            eql = " = ";
        else
            eql = " ";

        data = device.getPath() + "$"+command + eql + value + "$\r\n";

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

        data = device.getPath() + "$" + command + " ?$\r\n";

        response = sendData(data);
        HashMap<String,String> formattedData = responseParser(response);
        setDeviceData(device, formattedData);
        model.put(deviceIdentifier,device);

        return device;

    }


    public RS232Device populateDevice(String deviceIdentifier)
    {
        log.info("trying to find device for id " + deviceIdentifier + " in " + model);
        RS232Device device = model.get(deviceIdentifier);

        String result="";
        ArrayList<String> commands = getType(deviceIdentifier);


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

    public ArrayList<String> getType(String deviceIdentifier)
    {
        if(deviceIdentifier.equalsIgnoreCase("KINOS"))
            return commandsForKINOS_STATUS();
        else if (deviceIdentifier.equalsIgnoreCase("UNIDISK"))
            return commandsForUNIDISK_STATUS();
        else if (deviceIdentifier.equalsIgnoreCase("UNIDISKSC"))
            return commandsForUNIDISKSC_STATUS();
        else if (deviceIdentifier.equalsIgnoreCase("LR2"))
            return commandsForLR2_STATUS();
        else
            return commandsForLR2_STATUS();
    }


    private final static ArrayList<String> commandsForAMP_STATUS()
    {
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("$VOL ?$\r\n");
        commands.add("$BAL ?$\r\n");
        commands.add("$BAS ?$\r\n");
        commands.add("$TRB ?$\r\n");
        commands.add("$MUTE ?$\r\n");
        return commands;
    }

    private final static ArrayList<String> commandsForVERSION_STATUS()
    {
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("$VERSION SOFTWARE ?$\r\n");
        commands.add("$VERSION HARDWARE ?$\r\n");
        return commands;
    }

    private ArrayList<String> commandsForLR2_STATUS()
    {
        log.info("returning commands for RoomAmp2");
        ArrayList<String> commands = new ArrayList<String>();

        commands.addAll(commandsForVERSION_STATUS());

        commands.add("$ORIGIN ?$\r\n");
        commands.add("$LISTEN ?$\r\n");
        commands.add("$COUNTER POWER$\r\n");
        commands.add("$COUNTER MAINS$\r\n");
        commands.add("$STATUS$\r\n");

        commands.addAll(commandsForAMP_STATUS());

        return commands;
    }

    private ArrayList<String> commandsForKINOS_STATUS()
    {
        log.info("returning commands for KINOS");
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("$IR ?$\r\n");
        commands.add("$OSG ?$\r\n");
        commands.add("$QUIET ?$\r\n");

        commands.addAll(commandsForVERSION_STATUS());

        commands.add("$VOLUME LIMITS$\r\n");
        commands.add("$[BALANCE|BALANCE_LR] ?$\r\n");
        commands.add("$[BALANCE|BALANCE_LR] LIMITS$\r\n");
        commands.add("$LIPSYNC ?$\r\n");
        commands.add("$ LIPSYNC LIMITS$\r\n");
        commands.add("$SPEAKER SIZE speaker ?$\r\n");
        commands.add("$SPEAKER CALIBRATE speaker ?$\r\n");
        commands.add("$SPEAKER CALIBRATE LIMITS$\r\n");
        commands.add("$SPEAKER TRIM speaker ?$\r\n");
        commands.add("$SPEAKER TRIM LIMITS$\r\n");
        commands.add("$SURROUND ?$\r\n");
        commands.add("$INPUT PROFILE ?$\r\n");
        commands.add("$INPUT PROFILE LIMITS$\r\n");
        commands.add("$INPUT AUDIO ?$\r\n");
        commands.add("$INPUT VIDEO ?$\r\n");
        commands.add("$VIDEO PROGRESSIVE_SCAN [?|mode]$\r\n");
        commands.add("$VIDEO WATCH_DEFAULT [?|mode]$\r\n");
        commands.add("$RECORD ?$\r\n");
        commands.add("$PINKNOISE ?$\r\n");
        commands.add("$SYSTEM VOLUME ?$\r\n");
        commands.add("$SYSTEM STATUS$\r\n");
        commands.add("$COUNTER POWER ?$\r\n");
        commands.add("$COUNTER MAINS ?$\r\n");
        commands.add("$12V_TRIGGER number ?$\r\n");
        commands.add("$FAN ?$\r\n");

        commands.addAll(commandsForAMP_STATUS());

        return commands;
    }

    private ArrayList<String> commandsForUNIDISK_STATUS()
    {
        log.info("returning commands for UNIDISK");
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("$IR ?$\r\n");

        commands.addAll(commandsForVERSION_STATUS());

        commands.add("$MODE?$\r\n");
        commands.add("$TRACK ?$\r\n");
        commands.add("$TRACK TOT$\r\n");
        commands.add("$CHAPTER ?$\r\n");
        commands.add("$CHAPTER TOT$\r\n");
        commands.add("DISCINFO ?$\r\n");
        commands.add("NAMEINFO ?$\r\n");
        commands.add("NAMEINFO TRACK ?$\r\n");
        commands.add("NAMEINFO ARTIST ?$\r\n");
        commands.add("NAMEINFO ALBUM ?$\r\n");
        commands.add("$TIME ?$\r\n");
        commands.add("$PROGRAM ?$\r\n");
        commands.add("$REPEAT ?$\r\n");
        commands.add("$LAYER ?$\r\n");
        commands.add("$DISCID ?$\r\n");
        commands.add("$DISCTOC ?$\r\n");
        commands.add("$OPTION DISPLAY ?$\r\n");
        commands.add("$COUNTER POWER ?$\r\n");
        commands.add("$COUNTER MAINS ?$\r\n");

        commands.add("$OPTION IR_COMMANDS ?$\r\n");
        commands.add("$OPTION IR_RC5_OUT ?$\r\n");
        commands.add("$OPTION RC5_IN_OUT ?$\r\n");

        commands.add("$STATUS$\r\n");
        return commands;
    }

    private ArrayList<String> commandsForUNIDISKSC_STATUS()
    {
        log.info("returning commands for UNIDISK SC");
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("$IR ?$\r\n");

        commands.addAll(commandsForVERSION_STATUS());

        commands.add("$MODE$\r\n");
        commands.add("$TRACK ?$\r\n");
        commands.add("$TRACK TOT$\r\n");
        commands.add("$CHAPTER ?$\r\n");
        commands.add("$CHAPTER TOT$\r\n");
        commands.add("DISCINFO ?$\r\n");
        commands.add("NAMEINFO ?$\r\n");
        commands.add("NAMEINFO TRACK ?$\r\n");
        commands.add("NAMEINFO ARTIST ?$\r\n");
        commands.add("NAMEINFO ALBUM ?$\r\n");
        commands.add("$TIME DISC TOT$\r\n");
        commands.add("$TIME TRACK TOT$\r\n");
        commands.add("$TIME ?$\r\n");
        commands.add("$PROGRAM ?$\r\n");
        commands.add("$REPEAT ?$\r\n");
        commands.add("$LAYER ?$\r\n");
        commands.add("$DISCID$\r\n");
        commands.add("$DISCTOC$\r\n");

        commands.add("$COUNTER POWER ?$\r\n");
        commands.add("$COUNTER MAINS ?$\r\n");
        commands.add("$STATUS$\r\n");
        commands.add("$OPTION RS232_EVENTS ?$\r\n");
        commands.add("$OPTION RS232_STARTUP_MESSAGE ?$\r\n");

        // only SC
        commands.add("$LISTEN ?$\r\n");

        commands.addAll(commandsForAMP_STATUS());

        commands.add("$SURROUND ?$\r\n");
        commands.add("$TV TYPE ?$\r\n");
        commands.add("$SPEAKER SIZE FRONT ?$\r\n");
        commands.add("$SPEAKER SIZE CENTRE ?$\r\n");
        commands.add("$SPEAKER SIZE SURROUND ?$\r\n");
        commands.add("$SPEAKER SIZE SUBWOOFER ?$\r\n");
        commands.add("$SPEAKER TRIM FRONT_LEFT ?$\r\n");
        commands.add("$SPEAKER TRIM FRONT_RIGHT ?$\r\n");
        commands.add("$SPEAKER TRIM CENTRE ?$\r\n");
        commands.add("$SPEAKER TRIM SURROUND_LEFT ?$\r\n");
        commands.add("$SPEAKER TRIM SURROUND_RIGHT ?$\r\n");
        commands.add("$SPEAKER TRIM SUBWOOFER ?$\r\n");
        commands.add("$ASPECTRATIO ?$\r\n");
        commands.add("$NTSCTYPE ?$\r\n");
        commands.add("$CHANNELSETUP ?$\r\n");
        commands.add("$SPDIFOUTPUT ?$\r\n");
        commands.add("$PRIMARYDISPLAYDEVICE ?$\r\n");
        commands.add("$CLOSEDCAPTIONS ?$\r\n");
        commands.add("$DOWNMIX ?$\r\n");
        commands.add("$LPCMOUTPUT ?$\r\n");
        commands.add("$DUALMONO ?$\r\n");
        commands.add("$MIDNIGHTMOVIE ?$\r\n");

        //Knekt
        commands.add("$OPTION KNEKT_MODE ?$\r\n");
        commands.add("$OPTION KNEKT_AUDIO ?$\r\n");
        // returns only failures
        //commands.add("$OPTION KNEKT_LFE ?$\r\n");

        return commands;
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
        String[] b = id.split("\n");
        for(String c:b)
        {
            if(c!=null && c.trim().length()>1)
            {
                if(c.startsWith("!$ID") || c.startsWith("!$POLL ID")) // found ID Line
                {
                    //!$ID KINOS$
                    //!$POLL ID KINOS$
                    //!$POLL ID UNIDISK$
                    c = c.replace("!$ID","");
                    c = c.replace("!$POLL ID","");
                    c = c.replace("$","");
                    log.info(c);
                    return c.trim();
                }
            }
        }
        return "";

        /*
         !

        !$STANDBY OFF$

        !

        !$ID KINOS$


         */
        /*
        !

        !$POLL START$

        !

        !$POLL ID KINOS$


         */
        /*
         #KINOS#!

        #KINOS#!$POLL SLEEP$

        !

        !$POLL ID UNIDISK$



         */
    }

    private HashMap<String,String> responseParser(String response)
    {
        /*
        #KINOS#!

        #KINOS#!$SYSTEM VOLUME 100$


         */
        /*
        #KINOS#!$FAIL 16 3$
        #KINOS#!$INPUT VIDEO SVIDEO1$
        #KINOS#!$INPUT PROFILE 0 11$
        #UNIDISK#!$IGNORED CHAPTER PLAY_STOPPED$
         */
        /*
        @UNIDISK@$OPTION DISPLAY ?$
        is #UNIDISK#!

        #UNIDISK#!$DISCTOC TOTAL 8$

        #UNIDISK#$DISCTOC ENTRIES 713 478 637 663 480 493 388 512$

         */
        /*
        response for : @UNIDISK@$Mode?$
         is #UNIDISK#!

        #UNIDISK#!$VERSION HARDWARE PCAS317L2R2 B60000018E854614$

        #UNIDISK#!$VERSION HARDWARE PCAS546L1R1 5500000263351014$

        #UNIDISK#!$VERSION HARDWARE PCAS297L1R12 B50000011756B814$


         */
        /*
        response for : @UNIDISK@$VERSION HARDWARE ?$
         is #UNIDISK#!

        #UNIDISK#!$VERSION SOFTWARE H8 S1150210 ESS S1520208 MECH 03.03.00.00$


         */
        /*
        !$MUTE OFF$
         */
        ArrayList<String> content = new ArrayList<String>();
        HashMap<String,String> target = new HashMap<String, String>();

        String a = response;

        String[] b = a.split("\n");
        for(String c:b)
        {
            if(c!=null && c.trim().length()>1)
            {
                if(c.startsWith("#") && c.endsWith("#!"))
                    continue;
                if(c.contains("POLL"))
                    continue;

                log.info("working on " + c);
                if(c.contains("#!$"))
                    c = c.substring(c.indexOf("#!$")+1,c.length()-1);
                if(c.contains("#$"))
                    c = c.substring(c.indexOf("#$")+1,c.length()-1);

                log.info("removed device, rest " + c);
                c = c.replace("!","");
                c = c.replace("$","");
                content.add(c);
                log.info("added to content: " + c);
            }
        }

        for (String line:content)
        {
            String[] d = line.split(" ");

            if(d.length == 8 && d[0].startsWith("VERSION"))
            //e.g.   VERSION SOFTWARE H8 S1150210 ESS S1520208 MECH 03.03.00.00
            {
                log.info(d[0] + " " + d[1] + " " + d[2] + ":" + d[3]);
                log.info(d[0] + " " + d[1] + " " + d[4] + ":" + d[5]);
                log.info(d[0] + " " + d[1] + " " + d[6] + ":" + d[7]);
                target.put(d[0] + " " + d[1] + " " + d[2], d[3]);
                target.put(d[0] + " " + d[1] + " " + d[4] ,d[5]);
                target.put(d[0] + " " + d[1] + " " + d[6] ,d[7]);
            }
            else if(d.length == 4 && d[0].startsWith("VERSION"))
            //e.g.   VERSION HARDWARE PCAS317L2R2 B60000018E854614
            {
                log.info(d[0] + " " + d[1] + ":" + d[2] + " "+ d[3]);
                target.put(d[0] + " " + d[1], d[2] + " " + d[3]);
            }
            else if(d.length == 3)
            //e.g.   COUNTER MAINS 0:897:23:17
            {
                log.info(d[0] + " " + d[1] + ":" + d[2]);
                target.put(d[0] + " " + d[1], d[2]);
            }
            else if (d.length == 2)
            // e.g. BAL -2
            {
                log.info(d[0] + ":" + d[1]);
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

