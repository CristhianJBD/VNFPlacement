package py.una.pol;


import py.una.pol.service.MaOEAService;

public class NSGA3 {

    public static void main(String[] args) throws Exception {

        MaOEAService maOEAService = new MaOEAService();
        maOEAService.nsga3();
    }

}
