package py.una.pol.dto;

import lombok.Data;
import py.una.pol.dto.NFVdto.Node;
import py.una.pol.dto.NFVdto.Server;
import py.una.pol.dto.NFVdto.Vnf;

@Data
public class NodeGraph {

    //Nodo actual del grafo
    private Node node;

    //Nodo actualizado que debe cargarse al grafo
    private Node nodeUpdated;

    //Indica si se modifico o no el nodo del grafo
    private boolean updated;

    public NodeGraph(Node node) {
        this.node = node;
        this.nodeUpdated = new Node();
        this.nodeUpdated.setId(node.getId());
        this.nodeUpdated.setEnergyCost(node.getEnergyCost());
        if(node.getServer()!=null) {
            Server server = new Server();
            server.setId(node.getServer().getId());
            server.setEnergyPerCoreWatts(node.getServer().getEnergyPerCoreWatts());
            server.setEnergyCost(node.getServer().getEnergyCost());
            server.setEnergyPeakWatts(node.getServer().getEnergyPeakWatts());
            server.setEnergyUsed(node.getServer().getEnergyUsed());
            server.setResourceCPU(node.getServer().getResourceCPU());
            server.setResourceRAM(node.getServer().getResourceRAM());
            server.setResourceStorage(node.getServer().getResourceStorage());
            server.setResourceCPUUsed(node.getServer().getResourceCPUUsed());
            server.setResourceRAMUsed(node.getServer().getResourceRAMUsed());
            server.setResourceStorageUsed(node.getServer().getResourceStorageUsed());
            server.setLicenceCost(node.getServer().getLicenceCost());
            server.setResourceCPUCost(node.getServer().getResourceCPUCost());
            server.setResourceRAMCost(node.getServer().getResourceRAMCost());
            server.setResourceStorageCost(node.getServer().getResourceStorageCost());
            for(Vnf vnf : node.getServer().getVnf())
                server.getVnf().add(vnf);
            this.nodeUpdated.setServer(server);
        }
        this.updated = false;
    }
}
