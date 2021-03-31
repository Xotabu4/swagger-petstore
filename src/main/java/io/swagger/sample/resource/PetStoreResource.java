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
import io.swagger.sample.data.PetData;
import io.swagger.sample.data.StoreData;
import io.swagger.sample.exception.ApiException;
import io.swagger.sample.model.AbstractApiResponse;
import io.swagger.sample.model.Inventory;
import io.swagger.sample.model.Order;
import io.swagger.sample.util.AuthFilter;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.*;

@Path("/store")
@Api(value = "/store", tags = "store")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class PetStoreResource {
  static StoreData storeData = new StoreData();
  static PetData petData = new PetData();

  @GET
  @Path("/inventory")
  @Produces({ MediaType.APPLICATION_JSON })
  @ApiOperation(value = "Returns pet inventories by status", notes = "Returns a map of status codes to quantities", response = Inventory.class)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
      @ApiResponse(code = 401, message = "Must have admin permissions to access this endpoint") })
  public java.util.Map<String, Integer> getInventory(
      @ApiParam(value = "Admin only", required = true) @HeaderParam("token") @DefaultValue("") String token) throws ApiException {
    AuthFilter.assertAdmin(token);
    return petData.getInventoryByStatus();
  }

  @GET
  @Path("/order/{orderId}")
  @ApiOperation(value = "Find purchase order by ID", notes = "For valid response try integer IDs with value >= 1 and <= 10", response = Order.class)
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Must have admin permissions to access this endpoint"),
      @ApiResponse(code = 404, message = "Order not found") })
  public Response getOrderById(@ApiParam(value = "Admin only", required = true) @HeaderParam("token") @DefaultValue("") String token,
      @ApiParam(value = "ID of pet that needs to be fetched", required = true) @PathParam("orderId") Long orderId)
      throws ApiException {
    AuthFilter.assertAdmin(token);
    Order order = storeData.findOrderById(orderId);
    if (null != order) {
      return Response.ok().entity(order).build();
    }
    throw new ApiException(404, "Order not found");
  }

  @POST
  @Path("/order")
  @ApiOperation(value = "Place an order for a pet", consumes = "application/json", response = Order.class)
  @ApiResponses({ @ApiResponse(code = 401, message = "Must have user permissions to access this endpoint"),
      @ApiResponse(code = 400, message = "Wrong Order structure") })
  public Order placeOrder(@ApiParam(value = "User only", required = true) @HeaderParam("token") @DefaultValue("") String token,
      @ApiParam(value = "order placed for purchasing the pet", required = true) Order order) throws ApiException {
    AuthFilter.assertUser(token);
    if (order == null) {
      throw new ApiException(400, "Wrong Order structure");
    }
    return storeData.placeOrder(order);
  }

  @DELETE
  @Path("/order/{orderId}")
  @ApiOperation(value = "Delete purchase order by ID")
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Must have admin permissions to access this endpoint"),
      @ApiResponse(code = 404, message = "Order not found") })
  public Response deleteOrder(@ApiParam(value = "Admin only", required = true) @HeaderParam("token") @DefaultValue("") String token,
      @ApiParam(value = "ID of the order that needs to be deleted", required = true) @PathParam("orderId") Long orderId)
      throws ApiException {
    AuthFilter.assertAdmin(token);
    if (storeData.deleteOrder(orderId)) {
      return Response.ok().entity(new AbstractApiResponse(String.valueOf(orderId))).build();
    } else {
      throw new ApiException(404, "Order not found");
    }
  }
}
