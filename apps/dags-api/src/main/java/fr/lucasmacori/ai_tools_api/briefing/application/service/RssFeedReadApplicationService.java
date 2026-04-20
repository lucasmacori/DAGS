package fr.lucasmacori.ai_tools_api.briefing.application.service;

import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.briefing.domain.service.RssFeedReadService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class RssFeedReadApplicationService {

	private final RssFeedReadService rssFeedReadService;

	public void triggerReadAllFeeds() {
		Mono.fromRunnable(rssFeedReadService::readAllFeeds)
				.subscribeOn(Schedulers.boundedElastic())
				.subscribe();
	}
}
