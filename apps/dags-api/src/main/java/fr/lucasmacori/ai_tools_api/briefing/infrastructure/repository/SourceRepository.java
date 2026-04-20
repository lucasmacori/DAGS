package fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.ISourceRepository;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity.SourceEntity;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository.jdbc.SourceJDBCRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SourceRepository implements ISourceRepository {

	private final SourceJDBCRepository sourceJDBCRepository;

	@Override
	public List<Source> findAll() {
		return sourceJDBCRepository.findAll().stream().map(SourceEntity::toSource).toList();
	}

	@Override
	public Source create(Source source) {
		return sourceJDBCRepository.save(SourceEntity.fromNewSource(source)).toSource();
	}

	@Override
	public Optional<Source> findById(String sourceId) {
		return sourceJDBCRepository.findById(UUID.fromString(sourceId)).map(SourceEntity::toSource);
	}

	@Override
	public Source update(Source source) {
		return sourceJDBCRepository.save(SourceEntity.fromExistingSource(source)).toSource();
	}

	@Override
	public void deleteById(String sourceId) {
		sourceJDBCRepository.deleteById(UUID.fromString(sourceId));
	}
}
