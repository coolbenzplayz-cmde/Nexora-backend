package org.example.nexora.video;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    // UPLOAD
    @PostMapping("/upload")
    public Video upload(@RequestHeader("Authorization") String token,
                        @RequestBody Video video) {
        return videoService.upload(token, video);
    }

    // FEED
    @GetMapping
    public List<Video> getVideos() {
        return videoService.getAllVideos();
    }

    // VIEW (increments views)
    @PostMapping("/view/{id}")
    public Video view(@PathVariable Long id) {
        return videoService.viewVideo(id);
    }

    // LIKE
    @PostMapping("/like/{id}")
    public Video like(@PathVariable Long id) {
        return videoService.likeVideo(id);
    }
}