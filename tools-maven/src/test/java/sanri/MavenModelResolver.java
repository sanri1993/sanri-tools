/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package sanri;

import java.io.File;
import java.util.*;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

class MavenModelResolver implements ModelResolver {

	private final List<RemoteRepository> repositories;
	private final Set<String> repositoryIds;

	private final RepositorySystem system;
	private final RepositorySystemSession session;

	/**
	* Creates a new Maven repository resolver. This resolver uses service available to Maven to create an artifact
	* resolution chain
	*
	* @param system             the Maven based implementation of the {@link RepositorySystem}
	* @param session            the current Maven execution session
	* @param remoteRepositories the list of available Maven repositories
	*/
	public MavenModelResolver(RepositorySystem system, RepositorySystemSession session,
                              List<RemoteRepository> remoteRepositories) {
		this.system = system;
		this.session = session;

		// RemoteRepository is mutable
		this.repositories = new ArrayList<RemoteRepository>(remoteRepositories.size());
		for (final RemoteRepository remoteRepository : remoteRepositories) {
			this.repositories.add(new RemoteRepository.Builder(remoteRepository).build());
		}
		this.repositoryIds = new HashSet<String>(repositories.size());

		for (final RemoteRepository repository : repositories) {
			repositoryIds.add(repository.getId());
		}
	}

	/**
	* Cloning constructor
	*
	* @param origin
	*/
	private MavenModelResolver(MavenModelResolver origin) {
		this(origin.system, origin.session, origin.repositories);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.apache.maven.model.resolution.ModelResolver#addRepository(org.apache.
	 * maven.model.Repository)
	 */
	@Override
	public void addRepository(Repository repository) throws InvalidRepositoryException {
		if (repositoryIds.contains(repository.getId())) {
			return;
		}

		repositoryIds.add(repository.getId());
		repositories.add(
				new RemoteRepository.Builder(repository.getId(), repository.getName(), repository.getUrl()).build());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.maven.model.resolution.ModelResolver#newCopy()
	 */
	@Override
	public ModelResolver newCopy() {
		return new MavenModelResolver(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.apache.maven.model.resolution.ModelResolver#resolveModel(java.lang.
	 * String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public ModelSource resolveModel(String groupId, String artifactId, String version)
			throws UnresolvableModelException {
		Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, "", "pom", version);
		try {
			final ArtifactRequest request = new ArtifactRequest(pomArtifact, repositories, null);
			pomArtifact = system.resolveArtifact(session, request).getArtifact();

		} catch (ArtifactResolutionException e) {
			throw new UnresolvableModelException("Failed to resolve POM for " + groupId + ":" + artifactId + ":"
					+ version + " due to " + e.getMessage(), groupId, artifactId, version, e);
		}

		final File pomFile = pomArtifact.getFile();

		return new FileModelSource(pomFile);

	}

	public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
		// FIXME: Support version range. See DefaultModelResolver
		return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
	}

	@Override
	public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
		return resolveModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
	}

	@Override
	public void addRepository(final Repository repository, boolean replace) throws InvalidRepositoryException {
		if (session.isIgnoreArtifactDescriptorRepositories()) {
			return;
		}

		if (!repositoryIds.add(repository.getId())) {
			if (!replace) {
				return;
			}

			removeMatchingRepository(repository.getId());
		}
		repositories.add(
				new RemoteRepository.Builder(repository.getId(), repository.getName(), repository.getUrl()).build());
	}

	private void removeMatchingRepository(final String id) {
		Iterator<RemoteRepository> iterator = repositories.iterator();
		while (iterator.hasNext()) {
			RemoteRepository remoteRepository = iterator.next();
			if (remoteRepository.getId().equals(id)) {
				iterator.remove();
			}
		}
	}

}
