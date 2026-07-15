# MarkdownEditor

MarkdownEditor is a simple desktop Markdown editor built with Java Swing. It was originally created as a school project for DIT 102 at Rangsit University, and it is now being prepared as an open-source project for learning, experimentation, and future contributions.

## Overview

This application provides a lightweight way to write Markdown, preview it as HTML, and manage Markdown files from your local machine. The project focuses on a clean desktop experience while keeping the implementation simple and beginner-friendly.

## Details

[GitHub Pages](https://aungthuhein2005.github.io/MarkdownEditor/index.html)

## Features

- Create, open, save, and save-as Markdown files
- Live preview of Markdown as HTML
- Open a folder and browse project files in a tree view (last folder is remembered)
- Undo and redo support
- Unsaved-change tracking in the title bar
- Basic Markdown rendering with CommonMark support
- GFM table support
- Export the preview to PDF
- Modern desktop styling with FlatLaf

## Tech Stack

- Java
- Swing UI (with JavaFX WebView for live preview)
- CommonMark (`commonmark-java`) for Markdown parsing/rendering
- FlatLaf for look and feel
- openhtmltopdf / PDFBox for PDF export
- Apache Ant / NetBeans project structure
- jpackage for Windows installer packaging

## Project Structure

- src/markdowneditor/ - Main application entry point and core classes
- src/markdowneditor/ui/ - Swing UI components and main window
- src/markdowneditor/controller/ - Editor, preview, file tree, undo/redo, and PDF export logic
- src/markdowneditor/model/ - Data models used by the editor
- src/lib/ - Third-party libraries used by the project

## Getting Started

### End users (Windows)

Download the latest installer from the [Releases](https://github.com/aungthuhein2005/MarkdownEditor/releases) page and run it. The installer bundles a Java runtime, so a separate JDK is not required.

### Prerequisites (developers)

- JDK installed on your machine
- NetBeans IDE or any Java environment that can run Ant-based projects
- JavaFX SDK (for the live preview WebView)

### Running from source

1. Clone the repository:
   ```bash
   git clone https://github.com/aungthuhein2005/MarkdownEditor.git
   ```
2. Open the project in NetBeans or your preferred Java IDE.
3. Build and run the project.

### Build with Ant

If you are using the command line, you can build the project with:

```bash
ant
```

### Build a Windows installer

```powershell
powershell -ExecutionPolicy Bypass -File .\package-windows.ps1
```

The installer is written to `dist/installer/MarkdownEditor-1.0.0.exe`.

## Usage

- Open a Markdown file from the File menu (`File → Open`)
- Or open a folder with `File → Open Folder` to browse files in the sidebar
- Start typing in the editor pane; the preview pane updates as you go
- Save with `Ctrl+S`, or export the current preview to PDF from the Export menu

## Contributing

Contributions are welcome.

If you would like to improve the editor, please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

The app follows a light MVC split (`model/`, `controller/`, `ui/`). Opening the project in NetBeans is the fastest path if you need to edit the Swing forms.

## Roadmap

Possible future improvements include:

- Syntax highlighting for Markdown
- Dark mode support
- More Markdown extensions
- Better file explorer interactions
- Export to HTML

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
