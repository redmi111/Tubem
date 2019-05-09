package full.movie.tubem.player.extractor;

import full.movie.tubem.player.extractor.StreamingService.LinkType;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;

public class Newapp {
    private static final String TAG = Newapp.class.toString();
    private static Downloader downloader = null;

    private Newapp() {
    }

    public static StreamingService[] getServices() {
        return ServiceList.serviceList;
    }

    public static StreamingService getService(int serviceId) throws ExtractionException {
        StreamingService[] streamingServiceArr;
        for (StreamingService s : ServiceList.serviceList) {
            if (s.getServiceId() == serviceId) {
                return s;
            }
        }
        return null;
    }

    public static StreamingService getService(String serviceName) throws ExtractionException {
        return ServiceList.serviceList[getIdOfService(serviceName)];
    }

    public static String getNameOfService(int id) {
        try {
            return getService(id).getServiceInfo().name;
        } catch (Exception e) {
            System.err.println("Service id not known");
            e.printStackTrace();
            return "";
        }
    }

    public static int getIdOfService(String serviceName) {
        for (int i = 0; i < ServiceList.serviceList.length; i++) {
            if (ServiceList.serviceList[i].getServiceInfo().name.equals(serviceName)) {
                return i;
            }
        }
        return -1;
    }

    public static void init(Downloader d) {
        downloader = d;
    }

    public static Downloader getDownloader() {
        return downloader;
    }

    public static StreamingService getServiceByUrl(String url) {
        StreamingService[] streamingServiceArr;
        for (StreamingService s : ServiceList.serviceList) {
            if (s.getLinkTypeByUrl(url) != LinkType.NONE) {
                return s;
            }
        }
        return null;
    }
}
