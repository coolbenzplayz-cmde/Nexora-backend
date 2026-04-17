package org.example.nexora.video;

import org.example.nexora.security.JwtService;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.wallet.Wallet;
import org.example.nexora.wallet.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EarningRepository earningRepository;
    private final WalletRepository walletRepository;

    public VideoService(VideoRepository videoRepository,
                        JwtService jwtService,
                        UserRepository userRepository,
                        EarningRepository earningRepository,
                        WalletRepository walletRepository) {
        this.videoRepository = videoRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.earningRepository = earningRepository;
        this.walletRepository = walletRepository;
    }

    // 🎥 UPLOAD
    public Video upload(String token, Video video) {

        String email = jwtService.extractEmail(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        video = new Video(user.getId(), video.getTitle(), video.getVideoUrl());

        return videoRepository.save(video);
    }

    // 📺 GET ALL
    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    // 👀 VIEW + 💰 EARN
    public Video viewVideo(Long videoId) {

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // increase views
        video.setViews(video.getViews() + 1);

        // 💰 earning per view
        BigDecimal earningAmount = new BigDecimal("0.01");

        // save earning record
        Earning earning = new Earning(
                video.getUserId(),
                video.getId(),
                earningAmount
        );

        earningRepository.save(earning);

        // add to wallet
        Wallet wallet = walletRepository.findByUser_Id(video.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(earningAmount));
        walletRepository.save(wallet);

        return videoRepository.save(video);
    }

    // ❤️ LIKE
    public Video likeVideo(Long videoId) {

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        video.setLikes(video.getLikes() + 1);

        return videoRepository.save(video);
    }
}