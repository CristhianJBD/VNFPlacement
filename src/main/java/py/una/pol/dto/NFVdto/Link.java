package py.una.pol.dto.NFVdto;

import lombok.Data;

@Data
public class Link {

    //Identificador del Enlace
    private String id;

    //Delay del enlace
    private int delay;

    //Distancia del enlace
    private int distance;

    //Ancho de banda del enlace
    private double bandwidth;

    //Costo por unidad de Mbit que pasa por el enlace
    private double bandwidthCost;

    //Ancho de banda utilizada
    private double bandwidthUsed;

    //Cantidad de flujos que pasan por el link
    private int trafficAmount;

    public Link() {
    }

    public Link(Link link) {
        this.id = link.getId();
        this.delay = link.getDelay();
        this.distance = link.getDistance();
        this.bandwidth = link.getBandwidth();
        this.bandwidthCost = link.getBandwidthCost();
        this.bandwidthUsed = link.getBandwidthUsed();
        this.trafficAmount = link.getTrafficAmount();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Link: ");
        sb.append("id='").append(id).append('\'');
        sb.append(", delay=").append(delay);
        sb.append(", distance=").append(distance);
        sb.append(", bandwidth=").append(bandwidth);
        sb.append(", bandwidthCost=").append(bandwidthCost);
        sb.append(", bandwidthUsed=").append(bandwidthUsed);
        sb.append(", trafficAmount=").append(trafficAmount);
        return sb.toString();
    }
}

