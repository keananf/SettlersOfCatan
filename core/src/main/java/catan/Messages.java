// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package catan;

public final class Messages {
  private Messages() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface MessageOrBuilder extends
      // @@protoc_insertion_point(interface_extends:catan.Message)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.catan.Request request = 1;</code>
     */
    catan.Requests.Request getRequest();
    /**
     * <code>.catan.Request request = 1;</code>
     */
    catan.Requests.RequestOrBuilder getRequestOrBuilder();

    /**
     * <code>.catan.Event event = 2;</code>
     */
    catan.Events.Event getEvent();
    /**
     * <code>.catan.Event event = 2;</code>
     */
    catan.Events.EventOrBuilder getEventOrBuilder();

    public catan.Messages.Message.TypeCase getTypeCase();
  }
  /**
   * Protobuf type {@code catan.Message}
   */
  public  static final class Message extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:catan.Message)
      MessageOrBuilder {
    // Use Message.newBuilder() to construct.
    private Message(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private Message() {
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
    }
    private Message(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!input.skipField(tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              catan.Requests.Request.Builder subBuilder = null;
              if (typeCase_ == 1) {
                subBuilder = ((catan.Requests.Request) type_).toBuilder();
              }
              type_ =
                  input.readMessage(catan.Requests.Request.parser(), extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom((catan.Requests.Request) type_);
                type_ = subBuilder.buildPartial();
              }
              typeCase_ = 1;
              break;
            }
            case 18: {
              catan.Events.Event.Builder subBuilder = null;
              if (typeCase_ == 2) {
                subBuilder = ((catan.Events.Event) type_).toBuilder();
              }
              type_ =
                  input.readMessage(catan.Events.Event.parser(), extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom((catan.Events.Event) type_);
                type_ = subBuilder.buildPartial();
              }
              typeCase_ = 2;
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return catan.Messages.internal_static_catan_Message_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return catan.Messages.internal_static_catan_Message_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              catan.Messages.Message.class, catan.Messages.Message.Builder.class);
    }

    private int typeCase_ = 0;
    private java.lang.Object type_;
    public enum TypeCase
        implements com.google.protobuf.Internal.EnumLite {
      REQUEST(1),
      EVENT(2),
      TYPE_NOT_SET(0);
      private final int value;
      private TypeCase(int value) {
        this.value = value;
      }
      /**
       * @deprecated Use {@link #forNumber(int)} instead.
       */
      @java.lang.Deprecated
      public static TypeCase valueOf(int value) {
        return forNumber(value);
      }

      public static TypeCase forNumber(int value) {
        switch (value) {
          case 1: return REQUEST;
          case 2: return EVENT;
          case 0: return TYPE_NOT_SET;
          default: return null;
        }
      }
      public int getNumber() {
        return this.value;
      }
    };

    public TypeCase
    getTypeCase() {
      return TypeCase.forNumber(
          typeCase_);
    }

    public static final int REQUEST_FIELD_NUMBER = 1;
    /**
     * <code>.catan.Request request = 1;</code>
     */
    public catan.Requests.Request getRequest() {
      if (typeCase_ == 1) {
         return (catan.Requests.Request) type_;
      }
      return catan.Requests.Request.getDefaultInstance();
    }
    /**
     * <code>.catan.Request request = 1;</code>
     */
    public catan.Requests.RequestOrBuilder getRequestOrBuilder() {
      if (typeCase_ == 1) {
         return (catan.Requests.Request) type_;
      }
      return catan.Requests.Request.getDefaultInstance();
    }

    public static final int EVENT_FIELD_NUMBER = 2;
    /**
     * <code>.catan.Event event = 2;</code>
     */
    public catan.Events.Event getEvent() {
      if (typeCase_ == 2) {
         return (catan.Events.Event) type_;
      }
      return catan.Events.Event.getDefaultInstance();
    }
    /**
     * <code>.catan.Event event = 2;</code>
     */
    public catan.Events.EventOrBuilder getEventOrBuilder() {
      if (typeCase_ == 2) {
         return (catan.Events.Event) type_;
      }
      return catan.Events.Event.getDefaultInstance();
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (typeCase_ == 1) {
        output.writeMessage(1, (catan.Requests.Request) type_);
      }
      if (typeCase_ == 2) {
        output.writeMessage(2, (catan.Events.Event) type_);
      }
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (typeCase_ == 1) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, (catan.Requests.Request) type_);
      }
      if (typeCase_ == 2) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(2, (catan.Events.Event) type_);
      }
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof catan.Messages.Message)) {
        return super.equals(obj);
      }
      catan.Messages.Message other = (catan.Messages.Message) obj;

      boolean result = true;
      result = result && getTypeCase().equals(
          other.getTypeCase());
      if (!result) return false;
      switch (typeCase_) {
        case 1:
          result = result && getRequest()
              .equals(other.getRequest());
          break;
        case 2:
          result = result && getEvent()
              .equals(other.getEvent());
          break;
        case 0:
        default:
      }
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      switch (typeCase_) {
        case 1:
          hash = (37 * hash) + REQUEST_FIELD_NUMBER;
          hash = (53 * hash) + getRequest().hashCode();
          break;
        case 2:
          hash = (37 * hash) + EVENT_FIELD_NUMBER;
          hash = (53 * hash) + getEvent().hashCode();
          break;
        case 0:
        default:
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static catan.Messages.Message parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static catan.Messages.Message parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static catan.Messages.Message parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static catan.Messages.Message parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static catan.Messages.Message parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static catan.Messages.Message parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static catan.Messages.Message parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static catan.Messages.Message parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static catan.Messages.Message parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static catan.Messages.Message parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(catan.Messages.Message prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code catan.Message}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:catan.Message)
        catan.Messages.MessageOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return catan.Messages.internal_static_catan_Message_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return catan.Messages.internal_static_catan_Message_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                catan.Messages.Message.class, catan.Messages.Message.Builder.class);
      }

      // Construct using catan.Messages.Message.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        typeCase_ = 0;
        type_ = null;
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return catan.Messages.internal_static_catan_Message_descriptor;
      }

      public catan.Messages.Message getDefaultInstanceForType() {
        return catan.Messages.Message.getDefaultInstance();
      }

      public catan.Messages.Message build() {
        catan.Messages.Message result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public catan.Messages.Message buildPartial() {
        catan.Messages.Message result = new catan.Messages.Message(this);
        if (typeCase_ == 1) {
          if (requestBuilder_ == null) {
            result.type_ = type_;
          } else {
            result.type_ = requestBuilder_.build();
          }
        }
        if (typeCase_ == 2) {
          if (eventBuilder_ == null) {
            result.type_ = type_;
          } else {
            result.type_ = eventBuilder_.build();
          }
        }
        result.typeCase_ = typeCase_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof catan.Messages.Message) {
          return mergeFrom((catan.Messages.Message)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(catan.Messages.Message other) {
        if (other == catan.Messages.Message.getDefaultInstance()) return this;
        switch (other.getTypeCase()) {
          case REQUEST: {
            mergeRequest(other.getRequest());
            break;
          }
          case EVENT: {
            mergeEvent(other.getEvent());
            break;
          }
          case TYPE_NOT_SET: {
            break;
          }
        }
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        catan.Messages.Message parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (catan.Messages.Message) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int typeCase_ = 0;
      private java.lang.Object type_;
      public TypeCase
          getTypeCase() {
        return TypeCase.forNumber(
            typeCase_);
      }

      public Builder clearType() {
        typeCase_ = 0;
        type_ = null;
        onChanged();
        return this;
      }


      private com.google.protobuf.SingleFieldBuilderV3<
          catan.Requests.Request, catan.Requests.Request.Builder, catan.Requests.RequestOrBuilder> requestBuilder_;
      /**
       * <code>.catan.Request request = 1;</code>
       */
      public catan.Requests.Request getRequest() {
        if (requestBuilder_ == null) {
          if (typeCase_ == 1) {
            return (catan.Requests.Request) type_;
          }
          return catan.Requests.Request.getDefaultInstance();
        } else {
          if (typeCase_ == 1) {
            return requestBuilder_.getMessage();
          }
          return catan.Requests.Request.getDefaultInstance();
        }
      }
      /**
       * <code>.catan.Request request = 1;</code>
       */
      public Builder setRequest(catan.Requests.Request value) {
        if (requestBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          type_ = value;
          onChanged();
        } else {
          requestBuilder_.setMessage(value);
        }
        typeCase_ = 1;
        return this;
      }
      /**
       * <code>.catan.Request request = 1;</code>
       */
      public Builder setRequest(
          catan.Requests.Request.Builder builderForValue) {
        if (requestBuilder_ == null) {
          type_ = builderForValue.build();
          onChanged();
        } else {
          requestBuilder_.setMessage(builderForValue.build());
        }
        typeCase_ = 1;
        return this;
      }
      /**
       * <code>.catan.Request request = 1;</code>
       */
      public Builder mergeRequest(catan.Requests.Request value) {
        if (requestBuilder_ == null) {
          if (typeCase_ == 1 &&
              type_ != catan.Requests.Request.getDefaultInstance()) {
            type_ = catan.Requests.Request.newBuilder((catan.Requests.Request) type_)
                .mergeFrom(value).buildPartial();
          } else {
            type_ = value;
          }
          onChanged();
        } else {
          if (typeCase_ == 1) {
            requestBuilder_.mergeFrom(value);
          }
          requestBuilder_.setMessage(value);
        }
        typeCase_ = 1;
        return this;
      }
      /**
       * <code>.catan.Request request = 1;</code>
       */
      public Builder clearRequest() {
        if (requestBuilder_ == null) {
          if (typeCase_ == 1) {
            typeCase_ = 0;
            type_ = null;
            onChanged();
          }
        } else {
          if (typeCase_ == 1) {
            typeCase_ = 0;
            type_ = null;
          }
          requestBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>.catan.Request request = 1;</code>
       */
      public catan.Requests.Request.Builder getRequestBuilder() {
        return getRequestFieldBuilder().getBuilder();
      }
      /**
       * <code>.catan.Request request = 1;</code>
       */
      public catan.Requests.RequestOrBuilder getRequestOrBuilder() {
        if ((typeCase_ == 1) && (requestBuilder_ != null)) {
          return requestBuilder_.getMessageOrBuilder();
        } else {
          if (typeCase_ == 1) {
            return (catan.Requests.Request) type_;
          }
          return catan.Requests.Request.getDefaultInstance();
        }
      }
      /**
       * <code>.catan.Request request = 1;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          catan.Requests.Request, catan.Requests.Request.Builder, catan.Requests.RequestOrBuilder> 
          getRequestFieldBuilder() {
        if (requestBuilder_ == null) {
          if (!(typeCase_ == 1)) {
            type_ = catan.Requests.Request.getDefaultInstance();
          }
          requestBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              catan.Requests.Request, catan.Requests.Request.Builder, catan.Requests.RequestOrBuilder>(
                  (catan.Requests.Request) type_,
                  getParentForChildren(),
                  isClean());
          type_ = null;
        }
        typeCase_ = 1;
        onChanged();;
        return requestBuilder_;
      }

      private com.google.protobuf.SingleFieldBuilderV3<
          catan.Events.Event, catan.Events.Event.Builder, catan.Events.EventOrBuilder> eventBuilder_;
      /**
       * <code>.catan.Event event = 2;</code>
       */
      public catan.Events.Event getEvent() {
        if (eventBuilder_ == null) {
          if (typeCase_ == 2) {
            return (catan.Events.Event) type_;
          }
          return catan.Events.Event.getDefaultInstance();
        } else {
          if (typeCase_ == 2) {
            return eventBuilder_.getMessage();
          }
          return catan.Events.Event.getDefaultInstance();
        }
      }
      /**
       * <code>.catan.Event event = 2;</code>
       */
      public Builder setEvent(catan.Events.Event value) {
        if (eventBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          type_ = value;
          onChanged();
        } else {
          eventBuilder_.setMessage(value);
        }
        typeCase_ = 2;
        return this;
      }
      /**
       * <code>.catan.Event event = 2;</code>
       */
      public Builder setEvent(
          catan.Events.Event.Builder builderForValue) {
        if (eventBuilder_ == null) {
          type_ = builderForValue.build();
          onChanged();
        } else {
          eventBuilder_.setMessage(builderForValue.build());
        }
        typeCase_ = 2;
        return this;
      }
      /**
       * <code>.catan.Event event = 2;</code>
       */
      public Builder mergeEvent(catan.Events.Event value) {
        if (eventBuilder_ == null) {
          if (typeCase_ == 2 &&
              type_ != catan.Events.Event.getDefaultInstance()) {
            type_ = catan.Events.Event.newBuilder((catan.Events.Event) type_)
                .mergeFrom(value).buildPartial();
          } else {
            type_ = value;
          }
          onChanged();
        } else {
          if (typeCase_ == 2) {
            eventBuilder_.mergeFrom(value);
          }
          eventBuilder_.setMessage(value);
        }
        typeCase_ = 2;
        return this;
      }
      /**
       * <code>.catan.Event event = 2;</code>
       */
      public Builder clearEvent() {
        if (eventBuilder_ == null) {
          if (typeCase_ == 2) {
            typeCase_ = 0;
            type_ = null;
            onChanged();
          }
        } else {
          if (typeCase_ == 2) {
            typeCase_ = 0;
            type_ = null;
          }
          eventBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>.catan.Event event = 2;</code>
       */
      public catan.Events.Event.Builder getEventBuilder() {
        return getEventFieldBuilder().getBuilder();
      }
      /**
       * <code>.catan.Event event = 2;</code>
       */
      public catan.Events.EventOrBuilder getEventOrBuilder() {
        if ((typeCase_ == 2) && (eventBuilder_ != null)) {
          return eventBuilder_.getMessageOrBuilder();
        } else {
          if (typeCase_ == 2) {
            return (catan.Events.Event) type_;
          }
          return catan.Events.Event.getDefaultInstance();
        }
      }
      /**
       * <code>.catan.Event event = 2;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          catan.Events.Event, catan.Events.Event.Builder, catan.Events.EventOrBuilder> 
          getEventFieldBuilder() {
        if (eventBuilder_ == null) {
          if (!(typeCase_ == 2)) {
            type_ = catan.Events.Event.getDefaultInstance();
          }
          eventBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              catan.Events.Event, catan.Events.Event.Builder, catan.Events.EventOrBuilder>(
                  (catan.Events.Event) type_,
                  getParentForChildren(),
                  isClean());
          type_ = null;
        }
        typeCase_ = 2;
        onChanged();;
        return eventBuilder_;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }


      // @@protoc_insertion_point(builder_scope:catan.Message)
    }

    // @@protoc_insertion_point(class_scope:catan.Message)
    private static final catan.Messages.Message DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new catan.Messages.Message();
    }

    public static catan.Messages.Message getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<Message>
        PARSER = new com.google.protobuf.AbstractParser<Message>() {
      public Message parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new Message(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<Message> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<Message> getParserForType() {
      return PARSER;
    }

    public catan.Messages.Message getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_catan_Message_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_catan_Message_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016messages.proto\022\005catan\032\016requests.proto\032" +
      "\014events.proto\"S\n\007Message\022!\n\007request\030\001 \001(" +
      "\0132\016.catan.RequestH\000\022\035\n\005event\030\002 \001(\0132\014.cat" +
      "an.EventH\000B\006\n\004typeb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          catan.Requests.getDescriptor(),
          catan.Events.getDescriptor(),
        }, assigner);
    internal_static_catan_Message_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_catan_Message_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_catan_Message_descriptor,
        new java.lang.String[] { "Request", "Event", "Type", });
    catan.Requests.getDescriptor();
    catan.Events.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
