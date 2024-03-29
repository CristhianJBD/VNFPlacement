package py.una.pol.dto.NFVdto;

import lombok.Data;
import py.una.pol.dto.ResultPath;

import java.io.Serializable;

@Data
public class Traffic implements Serializable {
    private static final long serialVersionUID = 948617996701541930L;

    //Nodo origen
    private String nodeOriginId;

    //Nodo Destino
    private String nodeDestinyId;

    //Ancho de Banda Inicial
    private int bandwidth;

    //Maximo Delay permitido por el trafico de acuero al SLA
    private double delayMaxSLA;

    //Costo en dolares por falta del SLA
    private double penaltyCostSLO;

    //Cadena de Servicio (Secuencias de VNF)
    private SFC sfc;

    //Indica si el trafico fue procesado o no
    private boolean processed;

    //Solucion del trafico
    private ResultPath resultPath;

    //Cantidad de rechazos por sobrecarga de enlaces
    private int rejectLink;

    //Cantidad de rechazos por sobrecarga en los nodos
    private int rejectNode;

    public Traffic(Traffic traffic) {
        this.nodeOriginId = traffic.getNodeOriginId();
        this.nodeDestinyId = traffic.getNodeDestinyId();
        this.bandwidth = traffic.getBandwidth();
        this.delayMaxSLA = traffic.getDelayMaxSLA();
        this.penaltyCostSLO = traffic.getPenaltyCostSLO();
        this.sfc = traffic.getSfc();
        this.processed = traffic.isProcessed();
        this.resultPath = traffic.getResultPath();
        this.rejectLink = traffic.getRejectLink();
        this.rejectNode = traffic.getRejectNode();
    }

    public Traffic() {
    }

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

