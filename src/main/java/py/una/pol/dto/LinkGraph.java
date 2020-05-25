package py.una.pol.dto;

import lombok.Data;
import py.una.pol.dto.NFVdto.Link;

@Data
public class LinkGraph {

    // Enlaces actual del grafo
    private Link link;

    //Enlace actualizado que debe cargarse al grafo
    private Link linkUpdated;

    //Indica si se modifico o no el enlace del grafo
    private boolean updated;

    public LinkGraph(Link link) {
        this.link = link;
        this.linkUpdated = new Link();
        this.linkUpdated.setId(link.getId());
        this.linkUpdated.setDelay(link.getDelay());
        this.linkUpdated.setDistance(link.getDistance());
        this.linkUpdated.setBandwidth(link.getBandwidth());
        this.linkUpdated.setBandwidthCost(link.getBandwidthCost());
        this.linkUpdated.setBandwidthUsed(link.getBandwidthUsed());
        this.updated = false;
    }
}
