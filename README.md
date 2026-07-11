# MarkdownEditor

MarkdownEditor is a simple desktop Markdown editor built with Java Swing. It was originally created as a school project for DIT 102 at Rangsit University, and it is now being prepared as an open-source project for learning, experimentation, and future contributions.

## Overview

This application provides a lightweight way to write Markdown, preview it as HTML, and manage Markdown files from your local machine. The project focuses on a clean desktop experience while keeping the implementation simple and beginner-friendly.

## Features

- Create, open, save, and save-as Markdown files
- Live preview of Markdown as HTML
- Open a folder and browse project files in a tree view
- Undo and redo support
- Basic Markdown rendering with CommonMark support
- GFM table support
- Modern desktop styling with FlatLaf

## Tech Stack

- Java
- Swing UI
- CommonMark library for Markdown parsing/rendering
- Apache Ant / NetBeans project structure

## Project Structure

- src/markdowneditor/ - Main application entry point and core classes
- src/markdowneditor/ui/ - Swing UI components and main window
- src/markdowneditor/controller/ - Editor, preview, file tree, and undo/redo logic
- src/markdowneditor/model/ - Data models used by the editor
- src/lib/ - Third-party libraries used by the project

## Getting Started

### Prerequisites

- JDK installed on your machine
- NetBeans IDE or any Java environment that can run Ant-based projects

### Running the Application

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

## Usage

- Open a Markdown file from the File menu
- Start typing in the editor pane
- Use the preview pane to see the rendered output
- Open a folder to browse multiple files in the sidebar

## Contributing

Contributions are welcome.

If you would like to improve the editor, please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## Roadmap

Possible future improvements include:

- Syntax highlighting for Markdown
- Dark mode support
- More Markdown extensions
- Better file explorer interactions
- Export to PDF or HTML

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.