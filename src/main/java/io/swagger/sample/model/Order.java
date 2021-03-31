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

package io.swagger.sample.model;

import io.swagger.annotations.*;

import java.util.Date;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "Order")
public class Order {
  private long id;
  private long petId;
  private int quantity;
  private Date shipDate;
  private String status;
  private boolean complete;

  @XmlElement(name = "id")
  @ApiModelProperty(required = true)
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  
  @XmlElement(name = "completed")
  @ApiModelProperty(required = true)
  public boolean isComplete() {
    return complete;
  }

  public void setComplete(boolean complete) {
    this.complete = complete;
  }

  @XmlElement(name = "petId")
  @ApiModelProperty(required = true)
  public long getPetId() {
    return petId;
  }

  public void setPetId(long petId) {
    this.petId = petId;
  }

  @XmlElement(name = "quantity")
  @ApiModelProperty(required = true)
  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  @XmlElement(name = "status")
  @ApiModelProperty(required = true, value = "Order Status", allowableValues = "placed, approved, delivered")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @XmlElement(name = "shipDate")
  @ApiModelProperty(required = true)
  public Date getShipDate() {
    return shipDate;
  }

  public void setShipDate(Date shipDate) {
    this.shipDate = shipDate;
  }
}