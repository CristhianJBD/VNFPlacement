package py.una.pol.dto;

import lombok.Data;

@Data
public class Link {

    //Identificador del Enlace
    private String id;

    //Delay del enlace
    private Integer delay;

    //Distancia del enlace
    private Integer distance;

    //Ancho de banda del enlace
    private Integer bandwidth;

    //Costo por unidad de Mbit que pasa por el enlace
    private Integer bandwidthCost;

    //Ancho de banda utilizada
    private int bandwidthUsed;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Link: ");
        sb.append("id=").append(id);
        sb.append(", delay=").append(delay);
        sb.append(", distance=").append(distance);
        sb.append(", bandwidth=").append(bandwidth);
        sb.append(", bandwidthCost=").append(bandwidthCost);
        sb.append(", bandwidthUsed=").append(bandwidthUsed);
        return sb.toString();
    }
}

