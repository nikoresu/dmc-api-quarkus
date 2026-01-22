package com.nikoresu.dmc.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"id"})
public class DmcEntry {
    private String number;

    private String baseName;
    private String variation;

    private int red;
    private int green;
    private int blue;

    private String hex;

    @Builder.Default
    private List<String> similars = new ArrayList<>();

    private Long createdAt;
    private Long updatedAt;
}
