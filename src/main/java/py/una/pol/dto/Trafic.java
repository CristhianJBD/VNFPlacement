package py.una.pol.dto;

import lombok.Data;

@Data
public class Trafic {

    //Nodo origen
    private Node nodeOrigin;

    //Nodo Destino
    private Node nodeDestiny;

    //Ancho de Banda Inicial
    private Integer bandwidth;

    //Maximo Delay permitido por el trafico de acuero al SLA
    private Integer delayMaxSLA;

    //Costo en dolares por falta del SLA
    private Integer penaltyCostSLO;

    //Cadena de Servicio (Secuencias de VNF)
    private SFC sfc;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Trafic: ");
        sb.append("nodeOrigin=").append(nodeOrigin);
        sb.append(", nodeDestiny=").append(nodeDestiny);
        sb.append(", bandwidth=").append(bandwidth);
        sb.append(", delayMaxSLA=").append(delayMaxSLA);
        sb.append(", penaltyCostSLO=").append(penaltyCostSLO);
        sb.append(", sfc=").append(sfc);
        return sb.toString();
    }
}

