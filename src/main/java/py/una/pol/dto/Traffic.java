package py.una.pol.dto;

import lombok.Data;

@Data
public class Traffic {

    //Nodo origen
    private Node nodeOrigin;

    //Nodo Destino
    private Node nodeDestiny;

    //Ancho de Banda Inicial
    private int bandwidth;

    //Maximo Delay permitido por el trafico de acuero al SLA
    private int delayMaxSLA;

    //Costo en dolares por falta del SLA
    private double penaltyCostSLO;

    //Cadena de Servicio (Secuencias de VNF)
    private SFC sfc;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Traffic: ");
        sb.append("nodeOrigin=").append(nodeOrigin.getId());
        sb.append(", nodeDestiny=").append(nodeDestiny.getId());
        sb.append(", bandwidth=").append(bandwidth);
        sb.append(", delayMaxSLA=").append(delayMaxSLA);
        sb.append(", penaltyCostSLO=").append(penaltyCostSLO);
        sb.append(", sfc=").append(sfc);
        return sb.toString();
    }
}

