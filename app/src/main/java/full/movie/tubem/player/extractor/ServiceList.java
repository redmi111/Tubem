package full.movie.tubem.player.extractor;

import full.movie.tubem.player.extractor.services.youtube.YoutubeService;

class ServiceList {
    public static final StreamingService[] serviceList = {new YoutubeService(0)};

    ServiceList() {
    }
}
