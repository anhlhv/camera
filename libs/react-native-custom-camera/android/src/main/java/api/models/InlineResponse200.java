/*
 * OpenALPR Cloud API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 2.0.1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package api.models;

import java.util.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import api.models.InlineResponse200ProcessingTime;
import api.models.PlateDetails;
import api.models.RegionOfInterest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * InlineResponse200
 */
public class InlineResponse200 {
  @SerializedName("processing_time")
  private InlineResponse200ProcessingTime processingTime = null;

  @SerializedName("img_width")
  private Integer imgWidth = null;

  @SerializedName("img_height")
  private Integer imgHeight = null;

  @SerializedName("credit_cost")
  private Integer creditCost = null;

  @SerializedName("credits_monthly_used")
  private Integer creditsMonthlyUsed = null;

  @SerializedName("credits_monthly_total")
  private Integer creditsMonthlyTotal = null;

  @SerializedName("results")
  private List<PlateDetails> results = null;

  @SerializedName("regions_of_interest")
  private List<RegionOfInterest> regionsOfInterest = null;

  @SerializedName("epoch_time")
  private BigDecimal epochTime = null;

  @SerializedName("version")
  private Integer version = null;

  /**
   * Specifies the type of data in this response
   */
  @JsonAdapter(DataTypeEnum.Adapter.class)
  public enum DataTypeEnum {
    ALPR_RESULTS("alpr_results"),
    
    ALPR_GROUP("alpr_group"),
    
    HEARTBEAT("heartbeat");

    private String value;

    DataTypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static DataTypeEnum fromValue(String text) {
      for (DataTypeEnum b : DataTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public static class Adapter extends TypeAdapter<DataTypeEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final DataTypeEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public DataTypeEnum read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return DataTypeEnum.fromValue(String.valueOf(value));
      }
    }
  }

  @SerializedName("data_type")
  private DataTypeEnum dataType = null;

  public InlineResponse200 processingTime(InlineResponse200ProcessingTime processingTime) {
    this.processingTime = processingTime;
    return this;
  }

   /**
   * Get processingTime
   * @return processingTime
  **/
  @ApiModelProperty(value = "")
  public InlineResponse200ProcessingTime getProcessingTime() {
    return processingTime;
  }

  public void setProcessingTime(InlineResponse200ProcessingTime processingTime) {
    this.processingTime = processingTime;
  }

  public InlineResponse200 imgWidth(Integer imgWidth) {
    this.imgWidth = imgWidth;
    return this;
  }

   /**
   * Width of the uploaded image in pixels
   * @return imgWidth
  **/
  @ApiModelProperty(value = "Width of the uploaded image in pixels")
  public Integer getImgWidth() {
    return imgWidth;
  }

  public void setImgWidth(Integer imgWidth) {
    this.imgWidth = imgWidth;
  }

  public InlineResponse200 imgHeight(Integer imgHeight) {
    this.imgHeight = imgHeight;
    return this;
  }

   /**
   * Height of the input image in pixels
   * @return imgHeight
  **/
  @ApiModelProperty(value = "Height of the input image in pixels")
  public Integer getImgHeight() {
    return imgHeight;
  }

  public void setImgHeight(Integer imgHeight) {
    this.imgHeight = imgHeight;
  }

  public InlineResponse200 creditCost(Integer creditCost) {
    this.creditCost = creditCost;
    return this;
  }

   /**
   * The number of API credits that were used to process this image
   * @return creditCost
  **/
  @ApiModelProperty(value = "The number of API credits that were used to process this image")
  public Integer getCreditCost() {
    return creditCost;
  }

  public void setCreditCost(Integer creditCost) {
    this.creditCost = creditCost;
  }

  public InlineResponse200 creditsMonthlyUsed(Integer creditsMonthlyUsed) {
    this.creditsMonthlyUsed = creditsMonthlyUsed;
    return this;
  }

   /**
   * The number of API credits used this month
   * @return creditsMonthlyUsed
  **/
  @ApiModelProperty(value = "The number of API credits used this month")
  public Integer getCreditsMonthlyUsed() {
    return creditsMonthlyUsed;
  }

  public void setCreditsMonthlyUsed(Integer creditsMonthlyUsed) {
    this.creditsMonthlyUsed = creditsMonthlyUsed;
  }

  public InlineResponse200 creditsMonthlyTotal(Integer creditsMonthlyTotal) {
    this.creditsMonthlyTotal = creditsMonthlyTotal;
    return this;
  }

   /**
   * The maximum number of API credits available this month according to your plan
   * @return creditsMonthlyTotal
  **/
  @ApiModelProperty(value = "The maximum number of API credits available this month according to your plan")
  public Integer getCreditsMonthlyTotal() {
    return creditsMonthlyTotal;
  }

  public void setCreditsMonthlyTotal(Integer creditsMonthlyTotal) {
    this.creditsMonthlyTotal = creditsMonthlyTotal;
  }

  public InlineResponse200 results(List<PlateDetails> results) {
    this.results = results;
    return this;
  }

  public InlineResponse200 addResultsItem(PlateDetails resultsItem) {
    if (this.results == null) {
      this.results = new ArrayList<PlateDetails>();
    }
    this.results.add(resultsItem);
    return this;
  }

   /**
   * Get results
   * @return results
  **/
  @ApiModelProperty(value = "")
  public List<PlateDetails> getResults() {
    return results;
  }

  public void setResults(List<PlateDetails> results) {
    this.results = results;
  }

  public InlineResponse200 regionsOfInterest(List<RegionOfInterest> regionsOfInterest) {
    this.regionsOfInterest = regionsOfInterest;
    return this;
  }

  public InlineResponse200 addRegionsOfInterestItem(RegionOfInterest regionsOfInterestItem) {
    if (this.regionsOfInterest == null) {
      this.regionsOfInterest = new ArrayList<RegionOfInterest>();
    }
    this.regionsOfInterest.add(regionsOfInterestItem);
    return this;
  }

   /**
   * Describes the areas analyzed in the input image
   * @return regionsOfInterest
  **/
  @ApiModelProperty(value = "Describes the areas analyzed in the input image")
  public List<RegionOfInterest> getRegionsOfInterest() {
    return regionsOfInterest;
  }

  public void setRegionsOfInterest(List<RegionOfInterest> regionsOfInterest) {
    this.regionsOfInterest = regionsOfInterest;
  }

  public InlineResponse200 epochTime(BigDecimal epochTime) {
    this.epochTime = epochTime;
    return this;
  }

   /**
   * Epoch time that the image was processed in milliseconds
   * @return epochTime
  **/
  @ApiModelProperty(value = "Epoch time that the image was processed in milliseconds")
  public BigDecimal getEpochTime() {
    return epochTime;
  }

  public void setEpochTime(BigDecimal epochTime) {
    this.epochTime = epochTime;
  }

  public InlineResponse200 version(Integer version) {
    this.version = version;
    return this;
  }

   /**
   * API format version
   * @return version
  **/
  @ApiModelProperty(value = "API format version")
  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public InlineResponse200 dataType(DataTypeEnum dataType) {
    this.dataType = dataType;
    return this;
  }

   /**
   * Specifies the type of data in this response
   * @return dataType
  **/
  @ApiModelProperty(value = "Specifies the type of data in this response")
  public DataTypeEnum getDataType() {
    return dataType;
  }

  public void setDataType(DataTypeEnum dataType) {
    this.dataType = dataType;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse200 inlineResponse200 = (InlineResponse200) o;
    return Objects.equals(this.processingTime, inlineResponse200.processingTime) &&
        Objects.equals(this.imgWidth, inlineResponse200.imgWidth) &&
        Objects.equals(this.imgHeight, inlineResponse200.imgHeight) &&
        Objects.equals(this.creditCost, inlineResponse200.creditCost) &&
        Objects.equals(this.creditsMonthlyUsed, inlineResponse200.creditsMonthlyUsed) &&
        Objects.equals(this.creditsMonthlyTotal, inlineResponse200.creditsMonthlyTotal) &&
        Objects.equals(this.results, inlineResponse200.results) &&
        Objects.equals(this.regionsOfInterest, inlineResponse200.regionsOfInterest) &&
        Objects.equals(this.epochTime, inlineResponse200.epochTime) &&
        Objects.equals(this.version, inlineResponse200.version) &&
        Objects.equals(this.dataType, inlineResponse200.dataType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(processingTime, imgWidth, imgHeight, creditCost, creditsMonthlyUsed, creditsMonthlyTotal, results, regionsOfInterest, epochTime, version, dataType);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse200 {\n");
    
    sb.append("    processingTime: ").append(toIndentedString(processingTime)).append("\n");
    sb.append("    imgWidth: ").append(toIndentedString(imgWidth)).append("\n");
    sb.append("    imgHeight: ").append(toIndentedString(imgHeight)).append("\n");
    sb.append("    creditCost: ").append(toIndentedString(creditCost)).append("\n");
    sb.append("    creditsMonthlyUsed: ").append(toIndentedString(creditsMonthlyUsed)).append("\n");
    sb.append("    creditsMonthlyTotal: ").append(toIndentedString(creditsMonthlyTotal)).append("\n");
    sb.append("    results: ").append(toIndentedString(results)).append("\n");
    sb.append("    regionsOfInterest: ").append(toIndentedString(regionsOfInterest)).append("\n");
    sb.append("    epochTime: ").append(toIndentedString(epochTime)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    dataType: ").append(toIndentedString(dataType)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
  
}

