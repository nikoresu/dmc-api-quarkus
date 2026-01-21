package com.nikoresu.dmc.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DmcEntry {
    @JsonProperty("id")
    private String id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("description")
    private String description;

    @JsonProperty("thread_color")
    private String threadColor;

    @JsonProperty("created_at")
    private Long createdAt;  // Epoch time in milliseconds

    @JsonProperty("updated_at")
    private Long updatedAt;  // Epoch time in milliseconds
}
