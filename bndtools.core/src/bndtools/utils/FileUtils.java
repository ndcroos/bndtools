package bndtools.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import bndtools.Plugin;

public class FileUtils {
	public static IDocument readFully(IFile file) throws CoreException, IOException {
		if(file.exists()) {
			InputStream stream = file.getContents();
			byte[] bytes = readFully(stream);
			
			String string = new String(bytes, file.getCharset());
			return new Document(string);
		}
		return null;
	}
	
	public static byte[] readFully(InputStream stream) throws IOException {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			
			final byte[] buffer = new byte[1024];
			while(true) {
				int read = stream.read(buffer, 0, 1024);
				if(read == -1)
					break;
				output.write(buffer, 0, read);
			}
			return output.toByteArray();
		} finally {
			stream.close();
		}
	}
	public static void writeFully(IDocument document, IFile file, boolean createIfAbsent) throws CoreException, FileNotFoundException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(document.get().getBytes());
		if(file.exists()) {
			file.setContents(inputStream, false, true, null);
		} else {
			if(createIfAbsent)
				file.create(inputStream, false, null);
			else
				throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, "File does not exist: " + file.getFullPath().toString(), null));
		}
	}
	
	public static boolean isAncestor(File dir, File child) {
		if(child == null)
			return false;
		child = child.getAbsoluteFile();
		if(child.equals(dir))
			return true;
		return isAncestor(dir, child.getParentFile());
	}
	
	public static IResource toWorkspaceResource(File file) {
		IPath path = new Path(file.toString());
		
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IPath workspacePath = workspaceRoot.getLocation();
		
		if(workspacePath.isPrefixOf(path)) {
			final IPath relativePath = path.removeFirstSegments(workspacePath.segmentCount());
			IResource resource;
			if(file.isDirectory()) {
				resource = workspaceRoot.getFolder(relativePath);
			} else {
				resource = workspaceRoot.getFile(relativePath);
			}
			return resource;
		}
		return null;
	}
}