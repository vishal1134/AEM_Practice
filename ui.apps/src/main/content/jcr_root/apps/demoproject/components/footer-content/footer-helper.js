use(function () {
    var links = [];
    
    // Function to recursively find links across any JCR subnode level
    function findLinks(node) {
        var children = node.getChildren();
        var childIterator = children.iterator();
        
        while (childIterator.hasNext()) {
            var child = childIterator.next();
            var valueMap = child.adaptTo(Packages.org.apache.sling.api.resource.ValueMap);
            
            var label = valueMap.get("label", Packages.java.lang.String) 
                     || valueMap.get("linkLabel", Packages.java.lang.String)
                     || valueMap.get("linklabel", Packages.java.lang.String)
                     || valueMap.get("text", Packages.java.lang.String);
                     
            var url = valueMap.get("url", Packages.java.lang.String) 
                   || valueMap.get("linkUrl", Packages.java.lang.String)
                   || valueMap.get("linkurl", Packages.java.lang.String) 
                   || "#";

            if (label) {
                links.push({
                    label: label,
                    url: url
                });
            }
            
            // Check deeper child nodes
            if (child.hasChildren()) {
                findLinks(child);
            }
        }
    }

    findLinks(resource);

    return {
        links: links
    };
});