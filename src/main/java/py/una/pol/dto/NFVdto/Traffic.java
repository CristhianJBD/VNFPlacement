package py.una.pol.dto.NFVdto;

import lombok.Data;
import py.una.pol.dto.ResultPath;

@Data
public class Traffic {

    //Nodo origen
    private String nodeOriginId;

    //Nodo Destino
    private String nodeDestinyId;

    //Ancho de Banda Inicial
    private int bandwidth;

    //Maximo Delay permitido por el trafico de acuero al SLA
    private int delayMaxSLA;

    //Costo en dolares por falta del SLA
    private double penaltyCostSLO;

    //Cadena de Servicio (Secuencias de VNF)
    private SFC sfc;

    //Indica si el trafico fue procesado o no
    private boolean processed;

    //Solucion del trafico
    private ResultPath resultPath;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Traffic: ");
        sb.append("nodeOriginId=").append(nodeOriginId);
        sb.append(", nodeDestinyId=").append(nodeDestinyId);
        sb.append(", bandwidth=").append(bandwidth);
        sb.append(", delayMaxSLA=").append(delayMaxSLA);
        sb.append(", penaltyCostSLO=").append(penaltyCostSLO);
        sb.append(", sfc=").append(sfc);
        return sb.toString();
    }
}

