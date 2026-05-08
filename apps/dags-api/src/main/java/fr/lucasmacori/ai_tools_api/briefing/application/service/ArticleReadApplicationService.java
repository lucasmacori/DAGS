package fr.lucasmacori.ai_tools_api.briefing.application.service;

import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.briefing.domain.service.ArticleReadService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ArticleReadApplicationService {

	private final ArticleReadService articleReadService;

	public void triggerReadAllArticles(String userId) {
		Mono.fromRunnable(() -> articleReadService.readAllArticles(userId))
				.subscribeOn(Schedulers.boundedElastic())
				.subscribe();
	}
}
