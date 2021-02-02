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

import io.swagger.annotations.*;
import io.swagger.sample.data.UserData;
import io.swagger.sample.exception.ApiException;
import io.swagger.sample.exception.NotFoundException;
import io.swagger.sample.model.AbstractApiResponse;
import io.swagger.sample.model.User;
import io.swagger.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

@Path("/user")
@Api(value = "/user", description = "Operations about user")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class UserResource {
  static UserData userData = new UserData();

  private static Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

  @POST
  @ApiOperation(value = "Create user", notes = "This can only be done by the logged in user.", position = 1, consumes = "application/json", response = User.class)
  public Response createUser(@ApiParam(value = "Created user object", required = true) User user) {
    if (user == null) {
      return Response.status(405).entity(new AbstractApiResponse("no data")).build();
    }
    try {
      LOGGER.info("createUser ID {} STATUS {}", user.getId(), user.getUsername());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("createUser {}", Json.mapper().writeValueAsString(user));
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
    userData.addUser(user);
    return Response.ok().entity(user).build();
  }

  @PUT
  @ApiOperation(value = "Update user", notes = "This can only be done by the logged in user.", position = 4, consumes = "application/json", response = User.class)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid user supplied"),
      @ApiResponse(code = 404, message = "User not found") })
  public Response updateUser(@ApiParam(value = "Updated user object", required = true) User user) {
    if (user == null) {
      return Response.status(405).entity(new AbstractApiResponse("no data")).build();
    }
    userData.addUser(user);
    return Response.ok().entity(user).build();
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete user by id", notes = "This can only be done by the logged in user.", position = 5)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid user id supplied"),
      @ApiResponse(code = 404, message = "User not found") })
  public Response deleteUser(@ApiParam(value = "User id to delete", required = true) @PathParam("id") long id) {
    if (userData.removeUserById(id)) {
      return Response.ok().entity(new AbstractApiResponse(String.valueOf(id))).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("/{id}")
  @ApiOperation(value = "Get user by id", response = User.class, position = 0)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid id supplied"),
      @ApiResponse(code = 404, message = "User not found") })
  public Response getUserById(@ApiParam(value = "user id", required = true) @PathParam("id") Long userId)
      throws ApiException {
    LOGGER.debug("getUserById {}", userId);
    User user = userData.findUserById(userId);
    if (null != user) {
      return Response.ok().entity(user).build();
    } else {
      throw new NotFoundException(404, "User not found");
    }
  }

  @GET
  @Path("/login")
  @ApiOperation(value = "Logs user into the system", response = String.class, position = 6, responseHeaders = {
      @ResponseHeader(name = "X-Expires-After", description = "date in UTC when token expires", response = Date.class),
      @ResponseHeader(name = "X-Rate-Limit", description = "calls per hour allowed by the user", response = Integer.class) })
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid username/password supplied") })
  public Response loginUser(
      @ApiParam(value = "The user name for login", required = true) @QueryParam("username") String username,
      @ApiParam(value = "The password for login in clear text", required = true) @QueryParam("password") String password) {
    LOGGER.debug("loginUser {}", username);
    LOGGER.trace("loginUser {}", password);
    Date date = new Date(System.currentTimeMillis() + 3600000);
    return Response.ok().header("X-Expires-After", date.toString()).header("X-Rate-Limit", String.valueOf(5000))
        .entity(new AbstractApiResponse("logged in user session:" + System.currentTimeMillis())).build();
  }

  @GET
  @Path("/logout")
  @ApiOperation(value = "Logs out current logged in user session", position = 7)
  public Response logoutUser() {
    return Response.ok().entity(new AbstractApiResponse(String.valueOf("ok"))).build();
  }
}
