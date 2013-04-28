package de.audiogrid.devices.rs232.service;

import de.audiogrid.devices.rs232.model.Command;
import de.audiogrid.devices.rs232.model.RS232Device;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/devices")
public class RS232Resource {

    Log log = LogFactory.getLog(RS232Resource.class);
    SerialIO dao = SerialIO.getInstance();
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	//public List<RS232Device> findAll() {
	public List<RS232Device> findAll() {
		log.info("getDevices");
		return dao.getDevices();
	}

	@GET @Path("{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public RS232Device getDeviceByID(@PathParam("id") String id) {
		log.info("getDeviceByID " + id);
		return dao.getDeviceByID(id);
	}

    @GET @Path("populate/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public RS232Device populateDevice(@PathParam("id") String id) {
        log.info("populateDevice " + id);
        return dao.populateDevice(id);
    }

    @PUT @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public RS232Device update(Command command) {
        log.info("Updating device: " + command.getKey());
        dao.issueCommand(command.getDeviceIdentifier(),command.getKey(), command.getValue(),command.isIseql());
        return dao.getDeviceByID(command.getDeviceIdentifier());
    }
}
