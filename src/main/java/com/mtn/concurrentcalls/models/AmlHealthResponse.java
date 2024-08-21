package com.mtn.concurrentcalls.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmlHealthResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer statusCode;
	private String statusMessage;
	private String statusDescription;
	private String timestamp;
}
