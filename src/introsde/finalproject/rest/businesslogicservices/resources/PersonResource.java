package introsde.finalproject.rest.businesslogicservices.resources;

import introsde.finalproject.rest.businesslogicservices.util.UrlInfo;
import introsde.finalproject.rest.businesslogicservices.wrapper.CurrentMeasureList;
import introsde.finalproject.rest.businesslogicservices.wrapper.GoalList;
import introsde.finalproject.rest.businesslogicservices.wrapper.HistoryMeasureList;
import introsde.finalproject.rest.businesslogicservices.wrapper.MeasureList;
import introsde.finalproject.rest.businesslogicservices.model.Goal;
import introsde.finalproject.rest.businesslogicservices.model.Measure;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

@Stateless
@LocalBean
public class PersonResource {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	private UrlInfo urlInfo;
	private String storageServiceURL;

	private static String mediaType = MediaType.APPLICATION_JSON;
	
	ClientConfig clientConfig = new ClientConfig();
	
	/**
	 * initialize the connection with the Storage Service (SS)
	 */
	public PersonResource(UriInfo uriInfo, Request request) {
		this.uriInfo = uriInfo;
		this.request = request;

		this.urlInfo = new UrlInfo();
		this.storageServiceURL = urlInfo.getStorageURL();
	}

	private String errorMessage(Exception e) {
		return "{ \n \"error\" : \"Error in Business Logic Services, due to the exception: "
				+ e + "\"}";
	}

	private String externalErrorMessage(String e) {
		return "{ \n \"error\" : \"Error in External services, due to the exception: "
				+ e + "\"}";
	}

	// ******************* PERSON ***********************

	/**
	 * GET /businessLogic-service/person 
	 * 
	 * This method calls a getPersonList method in Storage Services Module
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response readPersonListDetails() {
		try {
			System.out
					.println("readPersonList: Reading list of details about all people from Storage Services Module in Business Logic Services...");

			String path = "/person";

			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(storageServiceURL + path);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			JSONObject obj = new JSONObject(result.toString());

			if (response.getStatusLine().getStatusCode() == 200) {
				return Response.ok(obj.toString()).build();
			} else {
				System.out
						.println("Storage Service Error response.getStatus() != 200");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(externalErrorMessage(response.toString()))
						.build();
			}
		} catch (Exception e) {
			System.out
					.println("Business Logic Service Error catch response.getStatus() != 200");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(errorMessage(e)).build();
		}
	}

	/**
	 * GET /businessLogic-service/person/{idPerson} 
	 * 
	 * This method calls a getPerson method in Storage Services Module
	 * @return
	 */
	@GET
	@Path("{pid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response readPersonDetails(@PathParam("pid") int idPerson) {
		try {
			System.out
					.println("readPerson: Reading Person with "
							+ idPerson
							+ " from Storage Services Module in Business Logic Services...");

			String path = "/person/" + idPerson;

			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(storageServiceURL + path);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			JSONObject o = new JSONObject(result.toString());

			if (response.getStatusLine().getStatusCode() == 200) {
				return Response.ok(o.toString()).build();
			} else {
				System.out
						.println("Storage Service Error response.getStatus() != 200");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(externalErrorMessage(response.toString()))
						.build();
			}

		} catch (Exception e) {
			System.out
					.println("Business Logic Service Error catch response.getStatus() != 200");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(errorMessage(e)).build();
		}

	}

	/**
	 * POST /businessLogic-service/person 
	 * 
	 * This method calls a createPerson method in Storage Services Module
	 * @return
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response insertNewPerson(String inputPersonJSON) {
		try {

			System.out
					.println("insertNewPerson: Inserting a new Person from Storage Services Module in Business Logic Services");

			String path = "/person";

			Client client = ClientBuilder.newClient(clientConfig);
			WebTarget service = client.target(storageServiceURL + path);
			Builder builder = service.request(mediaType);

			Response response = builder.post(Entity.json(inputPersonJSON));

			String result = response.readEntity(String.class);

			if (response.getStatus() != 201) {
				System.out
						.println("Storage Service Error response.getStatus() != 201");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(externalErrorMessage(response.toString()))
						.build();
			} else {
				return Response.ok(result).build();
			}
		} catch (Exception e) {
			System.out
					.println("Business Logic Service Error catch response.getStatus() != 201");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(errorMessage(e)).build();
		}
	}

	/**
	 * PUT /businessLogic-service/person/{idPerson} 
	 * 
	 * This method calls a updatePerson method in Storage Services Module
	 * @return
	 */
	@PUT
	@Path("{pid}")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response updatePerson(@PathParam("pid") int idPerson,
			String inputPersonJSON) {
		try {
			System.out
					.println("updatePerson: Updating Person from Storage Services Module in Business Logic Services");

			String path = "/person/" + idPerson;

			Client client = ClientBuilder.newClient(clientConfig);
			WebTarget webTarget = client.target(storageServiceURL + path);
			Builder builder = webTarget.request(mediaType);

			Response response = builder.put(Entity.json(inputPersonJSON));

			String result = response.readEntity(String.class);

			if (response.getStatus() != 200) {
				System.out
						.println("Storage Service Error response.getStatus() != 200");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(externalErrorMessage(response.toString()))
						.build();
			} else {
				return Response.ok(result).build();
			}
		} catch (Exception e) {
			System.out
					.println("Business Logic Service Error catch response.getStatus() != 200");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(errorMessage(e)).build();
		}
	}

	/**
	 * DELETE /businessLogic-service/person/{idPerson} 
	 * 
	 * This method calls adeletePerson method in Storage Services Module
	 * @return
	 */
	@DELETE
	@Path("{pid}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deletePerson(@PathParam("pid") int idPerson) {
		try {
			System.out
					.println("deletePerson: Deleting Person from Storage Services Module in Business Logic Services");

			String path = "/person/" + idPerson;

			
			Client client = ClientBuilder.newClient(clientConfig);
			WebTarget webTarget = client.target(storageServiceURL + path);
			Builder builder = webTarget.request(mediaType);

			Response response = builder.delete();

			String result = response.readEntity(String.class);

			if (response.getStatus() != 204) {
				System.out
						.println("Storage Service Error response.getStatus() != 204");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(externalErrorMessage(response.toString()))
						.build();
			} else {
				return Response.ok(result).build();
			}
		} catch (Exception e) {
			System.out
					.println("Business Logic Service Error catch response.getStatus() != 204");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(errorMessage(e)).build();
		}
	}

	/**
	 * GET /businessLogic-service/person/{idPerson}/current-health 
	 * 
	 * This method calls a getPerson method in Storage Services Module
	 * @return
	 */
	@GET
	@Path("{pid}/current-health")
	@Produces(MediaType.APPLICATION_JSON)
	public CurrentMeasureList readCurrentHealthDetails(@PathParam("pid") int idPerson) {
		try {
			System.out
					.println("readCurrentHealthDetails: Reading list of all current measures for a person with "
							+ idPerson
							+ " from Storage Services Module in Business Logic Services...");

			String path = "/person/" + idPerson;

			Client client = ClientBuilder.newClient(clientConfig);
			WebTarget service = client.target(storageServiceURL);
			Response response = service.path(path).request().accept(mediaType)
					.get(Response.class);
	    	
	    	String result = response.readEntity(String.class);
			
			JSONObject obj = new JSONObject(result);

			List<Measure> measureList = new ArrayList<Measure>();
			CurrentMeasureList cmwrapper = new CurrentMeasureList();

			if (response.getStatus() == 200) {

				JSONObject currentObj = (JSONObject) obj.get("currentHealth");
				JSONArray measureArr = (JSONArray)currentObj.getJSONArray("measure");
				
				for (int j = 0; j < measureArr.length(); j++) {
					Measure m = new Measure(measureArr.getJSONObject(j).getInt("mid"), 
											measureArr.getJSONObject(j).getString("name"), 
											measureArr.getJSONObject(j).getString("value"), 
											measureArr.getJSONObject(j).getString("created"));
					measureList.add(j, m);
				}
				
				cmwrapper.setCurrentMeasureList(measureList);
				return cmwrapper;

			} else {
				
				System.out.println("Storage Service Error response.getStatus() != 200");
				System.out.println("Didn't find any Person with  id = " + idPerson);
				cmwrapper.setCurrentMeasureList(measureList);
				return cmwrapper;
			}
			
		} catch (Exception e) {
			System.out.println("Business Logic Service Error catch response.getStatus() != 200");
			return null;
		}
	}

	
	/**
	 * GET /businessLogic-service/person/{idPerson}/history-health 
	 * 
	 * This method calls a getHistoryHealth method in Storage Services Module
	 * @return
	 */
	@GET
	@Path("{pid}/history-health")
	@Produces(MediaType.APPLICATION_JSON)
	public HistoryMeasureList readHistoryHealthDetails(@PathParam("pid") int idPerson) {
		
		List<Measure> measureList = new ArrayList<Measure>();
		HistoryMeasureList hmwrapper = new HistoryMeasureList();
		
		try {
			System.out
					.println("readHistoryHealthDetails: Reading list of all history measures for a person with "
							+ idPerson
							+ " from Storage Services Module in Business Logic Services...");

			String path = "/person/" + idPerson + "/historyHealth";

			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(storageServiceURL + path);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			JSONObject obj = new JSONObject(result.toString());
			
			if (response.getStatusLine().getStatusCode() == 200) {
				
				JSONArray measureArr = (JSONArray) obj.getJSONArray("measure");				
				System.out.println("MeasureLength: " + measureArr.length());
				
				for (int j = 0; j < measureArr.length(); j++) {
					Measure m = new Measure(measureArr.getJSONObject(j).getInt("mid"), 
											measureArr.getJSONObject(j).getString("name"), 
											measureArr.getJSONObject(j).getString("value"), 
											measureArr.getJSONObject(j).getString("created"));
					measureList.add(j, m);
				}
				
				hmwrapper.setHistoryMeasureList(measureList);
				
			}else{
				System.out.println("Storage Service Error response.getStatus() != 200");
				System.out.println("Didn't find any Person with  id = " + idPerson);
				hmwrapper.setHistoryMeasureList(measureList);	
			}
			
			return hmwrapper;
			
		} catch (Exception e) {
			System.out.println("Business Logic Service Error catch response.getStatus() != 200");
			System.out.println("Didn't find any Person with  id = " + idPerson);
			hmwrapper.setHistoryMeasureList(measureList);
			return hmwrapper;
		}
	}

	// ******************* MEASURE ***********************

	/**
	 * POST /businessLogic-service/person/idPerson/measure 
	 * 
	 * This method calls a createMeasure method in Storage Services Module
	 * @return
	 */
	@POST
	@Path("{pid}/measure")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response insertNewMeasure(@PathParam("pid") int idPerson, String inputMeasureJSON) {
		try {

			System.out
					.println("insertNewMeasure: Inserting a new Measure from Storage Services Module in Business Logic Services");

			String path = "/person/" + idPerson + "/measure";

			Client client = ClientBuilder.newClient(clientConfig);
			WebTarget service = client.target(storageServiceURL + path);
			Builder builder = service.request(mediaType);

			Response response = builder.post(Entity.json(inputMeasureJSON));

			String result = response.readEntity(String.class);

			if (response.getStatus() != 201) {
				System.out
						.println("Storage Service Error response.getStatus() != 201");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(externalErrorMessage(response.toString()))
						.build();
			} else {
				return Response.ok(result).build();
			}
		} catch (Exception e) {
			System.out
					.println("Business Logic Service Error catch response.getStatus() != 200");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(errorMessage(e)).build();
		}
	}
	
	
	/**
	 * GET /businessLogic-service/person/{idPerson}/measure/{measureName} 
	 * 
	 * This method calls a getMeasure method in Storage Services Module
	 * @return
	 */
	@GET
	@Path("{pid}/measure/{measureName}")
	@Produces(MediaType.APPLICATION_JSON)
	public MeasureList readMeasureListByMeasureName(@PathParam("pid") int idPerson,
			@PathParam("measureName") String measureName) {
		
		List<Measure> measureList = new ArrayList<Measure>();
		MeasureList mlwrapper = new MeasureList();
		
		try {
			System.out
					.println("readMeasureListByMeasureName: Reading list of all "
							+ measureName
							+ " for a person with "
							+ idPerson
							+ " from Storage Services Module in Business Logic Services...");

			String path = "/person/" + idPerson + "/measure/" + measureName;
			
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(storageServiceURL + path);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			JSONObject obj = new JSONObject(result.toString());
			
			if (response.getStatusLine().getStatusCode() == 200) {

				JSONArray measureArr = (JSONArray) obj.getJSONArray("measure");				
				for (int j = 0; j < measureArr.length(); j++) {
					Measure m = new Measure(measureArr.getJSONObject(j).getInt("mid"), 
											measureArr.getJSONObject(j).getString("name"), 
											measureArr.getJSONObject(j).getString("value"), 
											measureArr.getJSONObject(j).getString("created"));
					measureList.add(j, m);
				}
				mlwrapper.setMeasureList(measureList);
				
			}else{
				System.out.println("Storage Service Error response.getStatus() != 200");
				System.out.println("Didn't find any Person with  id = " + idPerson);
				mlwrapper.setMeasureList(measureList);
			}
			
			return mlwrapper;
			
		} catch (Exception e) {
			System.out.println("Business Logic Service Error catch response.getStatus() != 200");
			System.out.println("Didn't find any Person with  id = " + idPerson);
			mlwrapper.setMeasureList(measureList);
			return mlwrapper;
		}
	}

	
	// ******************* GOAL ***********************

	/**
	 * GET /businessLogic-service/person/{idPerson}/goal 
	 * 
	 * This method calls a getGoalList method in Storage Services Module
	 * @return
	 */
	@GET
	@Path("{pid}/goal")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public GoalList readGoalListDetails(@PathParam("pid") int idPerson) {
		
		List<Goal> goalList = new ArrayList<Goal>();
		GoalList glwrapper = new GoalList();
		
		try {
			System.out
					.println("readGoalListDetails: Reading list of all goals for Person with "
							+ idPerson
							+ " from Storage Services Module in Business Logic Services...");

			String path = "/person/" + idPerson + "/goal";

			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(storageServiceURL + path);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			JSONObject obj = new JSONObject(result.toString());

			if (response.getStatusLine().getStatusCode() == 200) {

				JSONArray goalArr = (JSONArray) obj.getJSONArray("goal");				
				for (int j = 0; j < goalArr.length(); j++) {
					Goal g = new Goal(goalArr.getJSONObject(j).getInt("gid"), 
											goalArr.getJSONObject(j).getString("type"), 
											goalArr.getJSONObject(j).getString("value"), 
											goalArr.getJSONObject(j).getString("startDateGoal"),
											goalArr.getJSONObject(j).getString("endDateGoal"),
											goalArr.getJSONObject(j).getBoolean("achieved"),
											goalArr.getJSONObject(j).getString("condition"));
					goalList.add(j, g);
				}
				glwrapper.setGoalList(goalList);
				
			}else{
				System.out.println("Storage Service Error response.getStatus() != 200");
				System.out.println("Didn't find any Person with  id = " + idPerson);
				glwrapper.setGoalList(goalList);
			}
			
			return glwrapper;
			
		} catch (Exception e) {
			System.out.println("Business Logic Service Error catch response.getStatus() != 200");
			System.out.println("Didn't find any Person with  id = " + idPerson);
			glwrapper.setGoalList(goalList);
			return glwrapper;
		}
	}

	
	/**
	 * GET /businessLogic-service/person/{idPerson}/goal/{measureName} 
	 * 
	 * This method calls a getGoal method in Storage Services Module
	 * @return
	 */
	@GET
	@Path("{pid}/goal/{measureName}")
	@Produces(MediaType.APPLICATION_JSON)
	public GoalList readGoalListByMeasureName(@PathParam("pid") int idPerson,
			@PathParam("measureName") String measureName) {
		
		List<Goal> goalList = new ArrayList<Goal>();
		GoalList glwrapper = new GoalList();
		
		try {
			System.out
					.println("readGoalListByMeasureName: Reading list of all "
							+ measureName
							+ " for a person with "
							+ idPerson
							+ " from Storage Services Module in Business Logic Services...");

			String path = "/person/" + idPerson + "/goal/" + measureName;
			
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(storageServiceURL + path);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			JSONObject obj = new JSONObject(result.toString());
			
			if (response.getStatusLine().getStatusCode() == 200) {

				JSONArray goalArr = (JSONArray) obj.getJSONArray("goal");
				
				for (int j = 0; j < goalArr.length(); j++) {
					Goal g = new Goal(goalArr.getJSONObject(j).getInt("gid"), 
											goalArr.getJSONObject(j).getString("type"), 
											goalArr.getJSONObject(j).getString("value"), 
											goalArr.getJSONObject(j).getString("startDateGoal"),
											goalArr.getJSONObject(j).getString("endDateGoal"),
											goalArr.getJSONObject(j).getBoolean("achieved"),
											goalArr.getJSONObject(j).getString("condition"));
					goalList.add(j, g);
				}
				glwrapper.setGoalList(goalList);
				
			}else{
				System.out.println("Storage Service Error response.getStatus() != 200");
				System.out.println("Didn't find any Person with  id = " + idPerson);
				glwrapper.setGoalList(goalList);
			}
			
			return glwrapper;
			
		} catch (Exception e) {
			System.out.println("Business Logic Service Error catch response.getStatus() != 200");
			System.out.println("Didn't find any Person with  id = " + idPerson);
			glwrapper.setGoalList(goalList);
			return glwrapper;
		}
	}
	
	
	/**
	 * POST /businessLogic-service/person/idPerson/goal 
	 * 
	 * This method calls a createGoal method in Storage Services Module
	 * @return
	 */
	@POST
	@Path("{pid}/goal")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response insertNewGoal(@PathParam("pid") int idPerson, String inputGoalJSON) {
		try {

			System.out
					.println("insertNewGoal: Inserting a new Goal from Storage Services Module in Business Logic Services");

			String path = "/person/" + idPerson + "/goal";

			Client client = ClientBuilder.newClient(clientConfig);
			WebTarget service = client.target(storageServiceURL + path);
			Builder builder = service.request(mediaType);

			Response response = builder.post(Entity.json(inputGoalJSON));

			String result = response.readEntity(String.class);

			if (response.getStatus() != 201) {
				System.out
						.println("Storage Service Error response.getStatus() != 201");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(externalErrorMessage(response.toString()))
						.build();
			} else {
				return Response.ok(result).build();
			}
		} catch (Exception e) {
			System.out
					.println("Business Logic Service Error catch response.getStatus() != 200");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(errorMessage(e)).build();
		}
	}
	
	
	/**
	 * This method returns a measure with {idMeasure}
	 * @return Measure a measure
	 */
	public Measure getMeasureById(int idPerson, int idMeasure) {
		System.out.println("getMeasureById: Reading Measures for idPerson "+ idPerson +"...");
		
		String path = "/person/" + idPerson + "/historyHealth";

		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget service = client.target(storageServiceURL);
		Response response = service.path(path).request().accept(mediaType)
				.get(Response.class);
		
		String result = response.readEntity(String.class);
		JSONObject obj = new JSONObject(result);
		
		List<Measure> measureList = new ArrayList<Measure>();
		JSONArray measureArr = (JSONArray)obj.getJSONArray("measure");
		
		for (int j = 0; j < measureArr.length(); j++) {
			Measure m = new Measure(measureArr.getJSONObject(j).getInt("mid"), 
									measureArr.getJSONObject(j).getString("name"), 
									measureArr.getJSONObject(j).getString("value"), 
									measureArr.getJSONObject(j).getString("created"));
			measureList.add(j, m);
		}
		
		for(int i=0; i<measureList.size(); i++){
			Measure m = measureList.get(i);
			if(m.getMid() == idMeasure){
				System.out.println("getMeasureById():\n" + m.toString());
				return m;
			}
		}
		return null;
	}

	/**
	 * This method compares endDateGoal date with today
	 * @param input
	 * @return -1 if input before todayDate, 0 if input is equal to todayDate , 1 if input is after todayDate
	 * @throws ParseException
	 */
	private int compareDateWithToday(String input) throws ParseException{
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = format.parse(input);
		Date todayDate = Calendar.getInstance().getTime();
		System.out.println("Compare date: " + input + " with today: " + format.format(todayDate) + " = " + date.compareTo(todayDate));
		return date.compareTo(todayDate);
	}
	
	/**
	 * This method updates a goal passed as param for a specified person with idPerson	
	 * @param idPerson
	 * @param goal
	 * @return
	 */
	private Integer updateGoal(int idPerson, Goal goal){
		System.out.println("updateGoal: Update goal "+ goal.getGid() +"...");
		String path = "/person/" + idPerson + "/goal/" + goal.getGid();

		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target(storageServiceURL + path);
		Builder builder = webTarget.request(mediaType);

		Response response = builder.put(Entity.entity(goal, mediaType));
		System.out.println("updateGoal():\n" + response);
		return response.readEntity(Integer.class);		
	}
	
	/**
	 * GET /businessLogic-service/person/{idPerson}/measure/{idMeasure}/check
	 *  
	 * Check if the goal is achieved for the measure passed as param
	 * @throws ParseException 
	 * 
	 */
	@GET
	@Path("{pid}/measure/{mid}/check")
	@Produces( MediaType.APPLICATION_JSON )
	public Boolean checkMeasureWithGoal(@PathParam("pid") int idPerson, @PathParam("mid") int idMeasure) throws ParseException {
		System.out.println("checkMeasureWithGoal: Checking measure "+ idMeasure +" for idPerson "+ idPerson +"...");
		
		Measure measure = getMeasureById(idPerson, idMeasure);
		System.out.println("Measure: \n" + measure.toString());
		
		GoalList listGoals = readGoalListByMeasureName(idPerson, measure.getName()); 
		System.out.println("ListGoal:");
		for(Goal goal: listGoals.getGoalList()){
			System.out.println(goal.toString());
		}
		
		Boolean result = false;
		
		if(listGoals.getGoalList().size() > 0){
			System.out.println("Lenght listGoals: " + listGoals.getGoalList().size());
			
			for(Goal goal : listGoals.getGoalList()){
				//the value 0 if x == y; a value less than 0 if x < y; and a value greater than 0 if x > y
				int count = Integer.compare(Integer.parseInt(measure.getValue()), Integer.parseInt(goal.getValue()));
				System.out.println("Count: " + count);
				
				String cond = goal.getCondition().replaceAll("\\s","");
				System.out.println("Condition: " + cond);
				
				//conditionGoal is set and the goal is not expired (then if endDateGoal is before todayDate means that function return -1 and goal is expired)
				if (goal.getCondition() != null && 
						compareDateWithToday(goal.getEndDateGoal()) >= 0 &&
						goal.isAchieved() == false) {
					if( (cond.equals("<") && count <  0) || (cond.equals("<=") && count <= 0) ||
						(cond.equals("=") && count == 0) || (cond.equals(">") && count > 0) ||
						(cond.equals(">=") && count >= 0)){
						//the goal is achieved
						goal.setAchieved(true);
						updateGoal(idPerson, goal);
						result = true;
					}else
						result = false;
				}
			}
		}
		return result;
	}
	
	
	/**
	 * GET /businessLogic-service/person/{idPerson}/motivational-goal/{measureName} 
	 * 
	 * This method calls a getPerson method in Storage Services Module
	 * @param idPerson
	 * @param measureName
	 * @return
	 */
	@GET
	@Path("{pid}/motivation-goal/{measureName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response readMotivationGoal(@PathParam("pid") int idPerson,
			@PathParam("measureName") String measureName) {
		try {
			System.out
					.println("motivationGoal: Checking goal for "
							+ measureName
							+ ", for a specified Person with "
							+ idPerson
							+ " from Storage Services Module in Business Logic Services...");

			String path = "/person/" + idPerson;
			String xmlBuild = "";
			Random generator = new Random();
			int randIndex = 0;

			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(storageServiceURL + path);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			JSONObject motivationGoalObj = new JSONObject(
					createJSONMotivationGoal());
			// System.out.println("lenght: "
			// + motivationGoalObj.getJSONArray("motivation-goal")
			// .length());

			JSONObject obj = new JSONObject(result.toString());

			xmlBuild = "<motivationGoal>";
			xmlBuild += "<person>" + obj.getString("lastname") + " "
					+ obj.getString("firstname") + "</person>";

			JSONArray goalArr = (JSONArray) obj.getJSONObject("goals")
					.getJSONArray("goal");

			JSONObject goal = null;

			for (int i = 0; i < goalArr.length(); i++) {
				if (goalArr.getJSONObject(i).getString("type")
						.equals(measureName)) {
					goal = goalArr.getJSONObject(i);
				}
			}

			if (goal == null) {
				xmlBuild += "<goal>" + "don't exist goal for " + measureName + "</goal>";
			} else {
				randIndex = generator.nextInt(motivationGoalObj.getJSONArray(
						"motivation-goal").length());
				System.out.println("index: " + randIndex);

				xmlBuild += "<goal>";
				xmlBuild += "<measure>" + goal.getString("type") + "</measure>";
				xmlBuild += "<value>" + goal.getString("value") + "</value>";
				xmlBuild += "<motivation>"
						+ motivationGoalObj.getJSONArray("motivation-goal")
								.getJSONObject(randIndex)
								.getString("motivation") + "</motivation>";
				xmlBuild += "<author>"
						+ motivationGoalObj.getJSONArray("motivation-goal")
								.getJSONObject(randIndex).getString("author")
						+ "</author>";
				xmlBuild += "</goal>";

			}

			xmlBuild += "</motivationGoal>";

			System.out.println(prettyXMLPrint(xmlBuild));

			JSONObject xmlJSONObj = XML.toJSONObject(xmlBuild);
			String jsonPrettyPrintString = xmlJSONObj.toString(4);

			return Response.ok(jsonPrettyPrintString).build();

		} catch (Exception e) {
			System.out
					.println("Business Logic Service Error catch response.getStatus() != 200");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(errorMessage(e)).build();
		}
	}

	/**
	 * GET /businessLogic-service/person/{idPerson}/motivation-health/{measureName} 
	 * 
	 * This method calls a getPerson method in Storage Services Module
	 * @param idPerson
	 * @param measureName
	 * @return
	 */
	@GET
	@Path("{pid}/motivation-health/{measureName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response readMotivationHealth(@PathParam("pid") int idPerson,
			@PathParam("measureName") String measureName) {
		try {
			System.out
					.println("readMotivationHealth: Checking measure for "
							+ measureName
							+ ", for a specified Person with "
							+ idPerson
							+ " from Storage Services Module in Business Logic Services...");

			String path = "/person/" + idPerson;
			String xmlBuild = "";
			Random generator = new Random();
			int randIndex = 0;

			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(storageServiceURL + path);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			JSONObject motivationHealthObj = new JSONObject(
					createJSONMotivationHealth());

			JSONObject obj = new JSONObject(result.toString());

			xmlBuild = "<motivationHealth>";
			xmlBuild += "<person>" + obj.getString("lastname") + " "
					+ obj.getString("firstname") + "</person>";

			JSONArray measureArr = (JSONArray) obj.getJSONObject(
					"currentHealth").getJSONArray("measure");

			JSONObject measure = null;

			for (int i = 0; i < measureArr.length(); i++) {
				if (measureArr.getJSONObject(i).getString("name")
						.equals(measureName)) {
					measure = measureArr.getJSONObject(i);
				}
			}
			if (measure == null) {
				xmlBuild += "<measure>" + measureName + " don't exist"
						+ "</measure>";
			} else {
				randIndex = generator.nextInt(motivationHealthObj.getJSONArray(
						"motivation-health").length());
				System.out.println("index: " + randIndex);

				xmlBuild += "<measure>";
				xmlBuild += "<name>" + measure.getString("name") + "</name>";
				xmlBuild += "<value>" + measure.getString("value") + "</value>";
				xmlBuild += "<motivation>"
						+ motivationHealthObj.getJSONArray("motivation-health")
								.getJSONObject(randIndex)
								.getString("motivation") + "</motivation>";
				xmlBuild += "<author>"
						+ motivationHealthObj.getJSONArray("motivation-health")
								.getJSONObject(randIndex).getString("author")
						+ "</author>";
				xmlBuild += "</measure>";

			}

			xmlBuild += "</motivationHealth>";

			System.out.println(prettyXMLPrint(xmlBuild));

			JSONObject xmlJSONObj = XML.toJSONObject(xmlBuild);
			String jsonPrettyPrintString = xmlJSONObj.toString(4);

			return Response.ok(jsonPrettyPrintString).build();

		} catch (Exception e) {
			System.out
					.println("Business Logic Service Error catch response.getStatus() != 200");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(errorMessage(e)).build();
		}

	}

	public static String createJSONMotivationHealth() {
		// create string array with motivation health - exercise - activity and
		// his author
		String[] motivationsHealth = {
				"Our health always seems much more valuable after we lose it.",
				"A man's health can be judged by which he takes two at a time - pills or stairs.",
				"Living a healthy lifestyle will only deprive you of poor health, lethargy, and fat.",
				"Health is a state of complete physical, mental and social well-being, and not merely the absence of disease or infirmity.",
				"Those who think they have not time for bodily exercise will sooner or later have to find time for illness.",
				"Movement is a medicine for creating change in a person's physical, emotional, and mental states.",
				"If it weren't for the fact that the TV set and the refrigerator are so far apart, some of us wouldn't get any exercise at all.",
				"Too many people confine their exercise to jumping to conclusions, running up bills, stretching the truth, bending over backwards, lying down on the job, sidestepping responsibility and pushing their luck.",
				"Fitness - If it came in a bottle, everybody would have a great body.",
				"Walking is the best possible exercise. Habituate yourself to walk very far.",
				"Walking: the most ancient exercise and still the best modern exercise." };

		String[] authors = { "Unknown", "Joan Welsh", "Jill Johnson",
				"World Health Organization", "Edward Stanley", "Carol Welch",
				"Joey Adams", "Anonymous", "Cher", "Thomas Jefferson",
				"Carrie Latet" };

		// create motivation quotes json obj
		JSONObject objInner;
		JSONArray arr = new JSONArray();
		for (int i = 0; i < motivationsHealth.length; i++) {
			objInner = new JSONObject();
			objInner.put("motivation", motivationsHealth[i]);
			objInner.put("author", authors[i]);
			arr.put(objInner);
		}
		JSONObject obj = new JSONObject();
		obj.put("motivation-health", arr);
		return obj.toString();
	}

	public static String createJSONMotivationGoal() {
		// create string array with motivation goal and his author
		String[] motivationsGoal = {
				"A goal without a plan is just a wish.",
				"Nothing can stop the man with the right mental attitude from achieving his goal; nothing on earth can help the man with the wrong mental attitude.",
				"If you want to accomplish anything in life, you can't just sit back and hope it will happen. You've got to make it happen.",
				"You must have long-range goals to keep you from being frustrated by short-range failures.",
				"If what you are doing is not moving you towards your goals, then it's moving you away from your goals.",
				"People with clear, written goals, accomplish far more in a shorter period of time than people without them could ever imagine.",
				"You cannot expect to achieve new goals or move beyond your present circumstances unless you change.",
				"This one step - choosing a goal and sticking to it - changes everything.",
				"Your goals, minus your doubts, equal your reality." };

		String[] authors = { "Larry Elder", "Thomas Jefferson", "Chuck Norris",
				"Charles C. Noble", "Brian Tracy", "Brian Tracy", "Les Brown",
				"Scott Reed", "Ralph Marston" };

		// create motivation quotes json obj
		JSONObject objInner;
		JSONArray arr = new JSONArray();
		for (int i = 0; i < motivationsGoal.length; i++) {
			//String sMeasure = motivationsGoal[i];
			//String tMeasure = sMeasure.replaceAll("", "-");
			objInner = new JSONObject();
			objInner.put("motivation", motivationsGoal[i]);
			objInner.put("author", authors[i]);
			arr.put(objInner);
		}
		JSONObject obj = new JSONObject();
		obj.put("motivation-goal", arr);
		return obj.toString();
	}

	/**
	 * Prints pretty format for XML
	 * 
	 * @param xml
	 * @return
	 * @throws TransformerException
	 */
	public String prettyXMLPrint(String xmlString) throws TransformerException {

		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");

		Source xmlInput = new StreamSource(new StringReader(xmlString));
		StringWriter stringWriter = new StringWriter();
		StreamResult xmlOutput = new StreamResult(stringWriter);

		transformer.transform(xmlInput, xmlOutput);
		return xmlOutput.getWriter().toString();
	}

}