package com.sanri.tools.modules.codepatch.service.dtos;

import com.dtflys.forest.annotation.Get;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *  diffEntry: DiffEntry[MODIFY src/main/java/com/sanri/app/servlet/SqlClientServlet.java]
 *  relativePath: com/sanri/app/servlet/SqlClientServlet.java
 *  modulePath: sanri-tools-maven
 *  compileFiles:
 *    - com/sanri/app/servlet/SqlClientServlet$1.class
 *    - com/sanri/app/servlet/SqlClientServlet.class
 *
 */
@ToString
@Setter
@Getter
public final class FileInfo {
	// 子模块路径
	private File modulePath;
	private DiffEntry diffEntry;
	private Path relativePath;
	private String baseName;
	private String extension;
	private Collection<File> compileFiles = new ArrayList<>();

	public FileInfo(DiffEntry diffEntry, Path relativePath) {
		this.diffEntry = diffEntry;
		this.relativePath = relativePath;
		this.baseName = FilenameUtils.getBaseName(relativePath.toFile().getName());
		this.extension = FilenameUtils.getExtension(relativePath.toFile().getName());
	}

	public FileInfo(DiffEntry diffEntry,Path relativePath, Collection<File> compileFiles) {
		this.diffEntry = diffEntry;
		this.relativePath = relativePath;
		this.compileFiles = compileFiles;
		this.baseName = FilenameUtils.getBaseName(relativePath.toFile().getName());
		this.extension = FilenameUtils.getExtension(relativePath.toFile().getName());
	}
}