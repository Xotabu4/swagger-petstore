/**
 *  Copyright 2016 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.swagger.sample.resource;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.swagger.annotations.*;
import io.swagger.sample.data.PetData;
import io.swagger.sample.exception.ApiException;
import io.swagger.sample.model.AbstractApiResponse;
import io.swagger.sample.model.Pet;
import io.swagger.sample.util.AuthFilter;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

@Path("/pet")
@Api(value = "/pet")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class PetResource {
  static PetData petData = new PetData();

  @GET
  @Path("/{petId}")
  @ApiOperation(value = "Find pet by ID", notes = "Returns a single pet", response = Pet.class)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
      @ApiResponse(code = 404, message = "Pet not found") })
  public Response getPetById(@ApiParam(value = "ID of pet to return") @PathParam("petId") Long petId)
      throws ApiException {
    Pet pet = petData.getPetById(petId);
    if (null != pet) {
      return Response.ok().entity(pet).build();
    }
    throw new ApiException(404, "Pet not found");
  }

  @DELETE
  @Path("/{petId}")
  @ApiOperation(value = "Deletes a pet", response = AbstractApiResponse.class)
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Must have admin permissions to access this endpoint"),
      @ApiResponse(code = 400, message = "Invalid ID supplied"), @ApiResponse(code = 404, message = "Pet not found") })
  public Response deletePet(@ApiParam(value = "Admin only", required = true) @HeaderParam("token") String token,
      @ApiParam(value = "Pet id to delete", required = true) @PathParam("petId") Long petId) throws ApiException {
    AuthFilter.assertAdmin(token);
    if (petData.deletePet(petId)) {
      return Response.ok().entity(new AbstractApiResponse(String.valueOf(petId))).build();
    }
    throw new ApiException(404, "Pet not found");
  }

  @POST
  @Path("/{petId}/uploadImage")
  @Consumes({ MediaType.MULTIPART_FORM_DATA })
  @Produces({ MediaType.APPLICATION_JSON })
  @ApiOperation(value = "uploads an image", response = AbstractApiResponse.class)
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Must have admin permissions to access this endpoint"),
      @ApiResponse(code = 500, message = "Cannot process file") })
  public Response uploadFile(@ApiParam(value = "Admin only", required = true) @HeaderParam("token") String token,
      @ApiParam(value = "ID of pet to update", required = true) @PathParam("petId") Long petId,
      @ApiParam(value = "file to upload") @FormDataParam("file") InputStream inputStream,
      @ApiParam(value = "file detail") @FormDataParam("file") FormDataContentDisposition fileDetail)
      throws ApiException {
    AuthFilter.assertAdmin(token);
    try {
      String uploadedFileLocation = "./" + fileDetail.getFileName();
      IOUtils.copy(inputStream, new FileOutputStream(uploadedFileLocation));
      String msg = "additionalMetadata: \nFile uploaded to " + uploadedFileLocation + ", "
          + (new java.io.File(uploadedFileLocation)).length() + " bytes";
      return Response.status(200).entity(new AbstractApiResponse(msg)).build();
    } catch (Exception e) {
      throw new ApiException(500, "Cannot process file");
    }
  }

  @POST
  @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  @ApiOperation(value = "Add a new pet to the store", notes = "Returns created pet", response = Pet.class)
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Must have admin permissions to access this endpoint"),
      @ApiResponse(code = 400, message = "Wrong pet structure") })
  public Response addPet(@ApiParam(value = "Admin only", required = true) @HeaderParam("token") String token,
      @ApiParam(value = "Pet object that needs to be added to the store", required = true) Pet pet)
      throws ApiException {
    AuthFilter.assertAdmin(token);
    if (pet == null) {
      throw new ApiException(400, "Wrong pet structure");
    }
    Pet updatedPet = petData.addPet(pet);
    return Response.ok().entity(updatedPet).build();
  }

  @PUT
  @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  @ApiOperation(value = "Update an existing pet", notes = "Returns updated pet", response = Pet.class)
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Must have admin permissions to access this endpoint"),
      @ApiResponse(code = 400, message = "Wrong pet structure") })
  public Response updatePet(@ApiParam(value = "Admin only", required = true) @HeaderParam("token") String token,
      @ApiParam(value = "Pet object that needs to be added to the store", required = true) Pet pet)
      throws ApiException {
    AuthFilter.assertAdmin(token);
    if (pet == null) {
      throw new ApiException(400, "Wrong pet structure");
    }
    Pet updatedPet = petData.addPet(pet);
    return Response.ok().entity(updatedPet).build();
  }

  @GET
  @Path("/findByStatus")
  @ApiOperation(value = "Finds Pets by status", notes = "Multiple status values can be provided with comma separated strings", response = Pet.class, responseContainer = "List")
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid status value") })
  public Response findPetsByStatus(
      @ApiParam(value = "Status values that need to be considered for filter", required = true, defaultValue = "available", allowableValues = "available,pending,sold", allowMultiple = true) @QueryParam("status") String status) {
    List<Pet> pets = petData.findPetByStatus(status);
    return Response.ok(pets.toArray(new Pet[pets.size()])).build();
  }

  @GET
  @Path("/findByTags")
  @ApiOperation(value = "Finds Pets by tags", notes = "Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.", response = Pet.class, responseContainer = "List")
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid tag value") })
  public Response findPetsByTags(
      @ApiParam(value = "Tags to filter by", required = true, allowMultiple = true) @QueryParam("tags") String tags) {
    List<Pet> pets = petData.findPetByTags(tags);
    return Response.ok(pets.toArray(new Pet[pets.size()])).build();
  }
}
