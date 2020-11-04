//package no.lindjo.util;
//
//
//import java.util.HashMap;
//
//public class TopicConfiguration {
//    public String name;
//    public short numReplicas;
//    public int numPartitions;
//    public boolean deleteTopic;
//    public HashMap<String, String> configuration;
//
//    TopicConfiguration(String name, short numReplicas, int numPartitions, boolean deleteTopic, HashMap<String, String> configuration) {
//        this.name = name;
//        this.numReplicas = numReplicas;
//        this.numPartitions = numPartitions;
//        this.deleteTopic = deleteTopic;
//        this.configuration = configuration;
//    }
//
//    public static TopicConfigurationBuilder builder() {
//        return new TopicConfigurationBuilder();
//    }
//
//    public String getName() {
//        return this.name;
//    }
//
//    public short getNumReplicas() {
//        return this.numReplicas;
//    }
//
//    public int getNumPartitions() {
//        return this.numPartitions;
//    }
//
//    public boolean isDeleteTopic() {
//        return this.deleteTopic;
//    }
//
//    public HashMap<String, String> getConfiguration() {
//        return this.configuration;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setNumReplicas(short numReplicas) {
//        this.numReplicas = numReplicas;
//    }
//
//    public void setNumPartitions(int numPartitions) {
//        this.numPartitions = numPartitions;
//    }
//
//    public void setDeleteTopic(boolean deleteTopic) {
//        this.deleteTopic = deleteTopic;
//    }
//
//    public void setConfiguration(HashMap<String, String> configuration) {
//        this.configuration = configuration;
//    }
//
//    public boolean equals(final Object o) {
//        if (o == this) return true;
//        if (!(o instanceof TopicConfiguration)) return false;
//        final TopicConfiguration other = (TopicConfiguration) o;
//        if (!other.canEqual((Object) this)) return false;
//        final Object this$name = this.getName();
//        final Object other$name = other.getName();
//        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
//        if (this.getNumReplicas() != other.getNumReplicas()) return false;
//        if (this.getNumPartitions() != other.getNumPartitions()) return false;
//        if (this.isDeleteTopic() != other.isDeleteTopic()) return false;
//        final Object this$configuration = this.getConfiguration();
//        final Object other$configuration = other.getConfiguration();
//        if (this$configuration == null ? other$configuration != null : !this$configuration.equals(other$configuration))
//            return false;
//        return true;
//    }
//
//    protected boolean canEqual(final Object other) {
//        return other instanceof TopicConfiguration;
//    }
//
//    public int hashCode() {
//        final int PRIME = 59;
//        int result = 1;
//        final Object $name = this.getName();
//        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
//        result = result * PRIME + this.getNumReplicas();
//        result = result * PRIME + this.getNumPartitions();
//        result = result * PRIME + (this.isDeleteTopic() ? 79 : 97);
//        final Object $configuration = this.getConfiguration();
//        result = result * PRIME + ($configuration == null ? 43 : $configuration.hashCode());
//        return result;
//    }
//
//    public String toString() {
//        return "TopicConfiguration(name=" + this.getName() + ", numReplicas=" + this.getNumReplicas() + ", numPartitions=" + this.getNumPartitions() + ", deleteTopic=" + this.isDeleteTopic() + ", configuration=" + this.getConfiguration() + ")";
//    }
//
//    public static class TopicConfigurationBuilder {
//        private String name;
//        private short numReplicas;
//        private int numPartitions;
//        private boolean deleteTopic;
//        private HashMap<String, String> configuration;
//
//        TopicConfigurationBuilder() {
//        }
//
//        public TopicConfigurationBuilder name(String name) {
//            this.name = name;
//            return this;
//        }
//
//        public TopicConfigurationBuilder numReplicas(short numReplicas) {
//            this.numReplicas = numReplicas;
//            return this;
//        }
//
//        public TopicConfigurationBuilder numPartitions(int numPartitions) {
//            this.numPartitions = numPartitions;
//            return this;
//        }
//
//        public TopicConfigurationBuilder deleteTopic(boolean deleteTopic) {
//            this.deleteTopic = deleteTopic;
//            return this;
//        }
//
//        public TopicConfigurationBuilder configuration(HashMap<String, String> configuration) {
//            this.configuration = configuration;
//            return this;
//        }
//
//        public TopicConfiguration build() {
//            return new TopicConfiguration(name, numReplicas, numPartitions, deleteTopic, configuration);
//        }
//
//        public String toString() {
//            return "TopicConfiguration.TopicConfigurationBuilder(name=" + this.name + ", numReplicas=" + this.numReplicas + ", numPartitions=" + this.numPartitions + ", deleteTopic=" + this.deleteTopic + ", configuration=" + this.configuration + ")";
//        }
//    }
//}