package com.demoproject.core.models;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CardModel {

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String description;

    @ValueMapValue
    private String buttonText;

    @ValueMapValue
    private String buttonLink;

    @ValueMapValue
    private String fileReference;

    @ChildResource(name = "cardItems")
    private List<CardItem> cardItems;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getButtonText() {
        return buttonText;
    }

    public String getButtonLink() {
        return buttonLink;
    }

    public String getFileReference() {
        return fileReference;
    }

    public List<CardItem> getCardItems() {
        return cardItems;
    }
}