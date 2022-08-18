package com.sanri.tools.modules.proxy.service.dtos;

import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
public class RequestInfo {
    private String id;
    private String comment;
    private RequestLine requestLine;
    private Message message;
    private String script;

    public RequestInfo() {
    }

    public RequestInfo(String id, RequestLine requestLine) {
        this.id = id;
        this.requestLine = requestLine;
    }

    public RequestInfo(String id, RequestLine requestLine, Message message) {
        this.id = id;
        this.requestLine = requestLine;
        this.message = message;
    }

    @Data
    public static final class Message{
        private List<Header> headers = new ArrayList<>();
        private Body body;

        public Message() {
        }

        public Message(List<Header> headers, Body body) {
            this.headers = headers;
            this.body = body;
        }
    }

    @Data
    public static final class Header{
        private String field;
        private String value;

        public Header() {
        }

        public Header(String field, String value) {
            this.field = field;
            this.value = value;
        }
    }

    public interface Body{

    }

    @Data
    public static final class TextBody implements Body{
        private String content;

        public TextBody() {
        }

        public TextBody(String content) {
            this.content = content;
        }
    }

    @Data
    public static final class FileBody implements Body{
        private String filename;
        private File file;

        public FileBody() {
        }

        public FileBody(File file) {
            this.file = file;
        }

        public FileBody(String filename, File file) {
            this.filename = filename;
            this.file = file;
        }
    }

    @Data
    public static final class MultipartBody implements Body{
        private String boundary;
        private List<Message> messages = new ArrayList<>();

        public MultipartBody() {
        }

        public MultipartBody(String boundary, List<Message> messages) {
            this.boundary = boundary;
            this.messages = messages;
        }
    }

    @Data
    public static final class RequestLine{
        private String method;
        private String url;

        public RequestLine() {
        }

        public RequestLine(String method, String url) {
            this.method = method;
            this.url = url;
        }
    }
}
