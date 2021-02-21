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
import io.swagger.sample.model.AbstractApiResponse;
import io.swagger.sample.model.User;
import io.swagger.sample.util.AuthFilter;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/user")
@Api(value = "/user", description = "Operations about user")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class UserResource {
  static UserData userData = new UserData();

  @POST
  @Path("/register")
  @ApiOperation(value = "Register user", notes = "Register new user", consumes = "application/json", response = User.class)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Wrong user structure") })
  public Response registerUser(@ApiParam(value = "User to create", required = true) User user) throws ApiException {
    if (user == null) {
      throw new ApiException(400, "Wrong user structure");
    }
    userData.addUser(user);
    return Response.ok().entity(user).build();
  }

  @POST
  @Path("/create")
  @ApiOperation(value = "Create user", notes = "This can only be done by admin", consumes = "application/json", response = User.class)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Wrong user structure") })
  public Response createUser(@ApiParam() @HeaderParam("token") String token,
      @ApiParam(value = "User to create", required = true) User user) throws ApiException {
    AuthFilter.assertAdmin(token);
    if (user == null) {
      throw new ApiException(400, "Wrong user structure");
    }
    userData.addUser(user);
    return Response.ok().entity(user).build();
  }

  @PUT
  @ApiOperation(value = "Update user", notes = "This can only be done by the logged in user.", consumes = "application/json", response = User.class)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid user supplied"),
      @ApiResponse(code = 404, message = "User not found") })
  public Response updateUser(@ApiParam() @HeaderParam("token") String token,
      @ApiParam(value = "Updated user object", required = true) User user) throws ApiException {
    AuthFilter.assertAdmin(token);
    if (user == null) {
      throw new ApiException(404, "User not found");
    }
    userData.addUser(user);
    return Response.ok().entity(user).build();
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete user by id", notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { @ApiResponse(code = 404, message = "User not found") })
  public Response deleteUser(@ApiParam() @HeaderParam("token") String token,
      @ApiParam(value = "User id to delete", required = true) @PathParam("id") long id) throws ApiException {
    AuthFilter.assertAdmin(token);
    if (userData.removeUserById(id)) {
      return Response.ok().entity(new AbstractApiResponse(String.valueOf(id))).build();
    } else {
      throw new ApiException(404, "User not found");
    }
  }

  @GET
  @Path("/{id}")
  @ApiOperation(value = "Get user by id", response = User.class)
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Must have admin permissions to access this endpoint"),
      @ApiResponse(code = 404, message = "User not found") })
  public Response getUserById(@ApiParam() @HeaderParam("token") String token,
      @ApiParam(value = "user id", required = true) @PathParam("id") Long userId) throws ApiException {

    AuthFilter.assertAdmin(token);
    User user = userData.findUserById(userId);
    if (null != user) {
      return Response.ok().entity(user).build();
    } else {
      throw new ApiException(404, "User not found");
    }
  }

  @GET
  @Path("/login")
  @ApiOperation(value = "Logs user into the system. Use admin/admin or user/user credentials", response = AbstractApiResponse.class, responseHeaders = {
      @ResponseHeader(name = "token", response = String.class) })
  @ApiResponses(value = { @ApiResponse(code = 403, message = "Invalid username/password supplied") })
  public Response loginUser(
      @ApiParam(value = "The user name for login", required = true) @QueryParam("username") String username,
      @ApiParam(value = "The password for login in clear text", required = true) @QueryParam("password") String password)
      throws ApiException {
    if (username == "admin" && password == "admin") {
      return Response.ok().header("token", "admin-token").entity(new AbstractApiResponse("Admin Authorized!")).build();
    } else if (username == "user" && password == "user") {
      return Response.ok().header("token", "user-token").entity(new AbstractApiResponse("User Authorized!")).build();
    }
    throw new ApiException(401, "Invalid username/password supplied");
  }

  @GET
  @Path("/logout")
  @ApiOperation(value = "Logs out current logged in user session")
  public Response logoutUser() {
    return Response.ok().entity(new AbstractApiResponse(String.valueOf("ok"))).build();
  }
}
