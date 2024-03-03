create table role_permission_mapping (
    role_type     varchar(255) not null,
    permission_id varchar(255) not null,
    primary key (role_type, permission_id)
);

create index FKd2egdvfl1ffawaxp0dec1y508
    on role_permission_mapping (permission_id);

INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '02c72178-bac2-424c-aa38-1c60a2aa1d6e');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '02c72178-bac2-424c-aa38-1c60a2aa1d6e');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '07a13959-7044-4f7c-bf4c-661853871700');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '07a13959-7044-4f7c-bf4c-661853871700');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '0dd329a2-227d-4980-a951-2789d0373226');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '0dd329a2-227d-4980-a951-2789d0373226');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '186c3767-02af-48b8-84a1-24cdabc19961');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '186c3767-02af-48b8-84a1-24cdabc19961');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '2c9e517f-6edf-4cc9-a5e6-a582a87c3753');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '2c9e517f-6edf-4cc9-a5e6-a582a87c3753');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '2e23a0dc-ecc2-448e-92d5-09d698553d31');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '2e23a0dc-ecc2-448e-92d5-09d698553d31');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '4be6129d-b3b0-41df-9fcc-946ab36545dc');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '4be6129d-b3b0-41df-9fcc-946ab36545dc');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '4d0be824-2e7b-48fa-aba3-a1a37c452e23');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '4d0be824-2e7b-48fa-aba3-a1a37c452e23');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '58bed2fa-ca97-411c-b511-e7bf31a435cc');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '67b26d13-8373-4264-ba2a-4d8201ceebb0');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '6aa6faef-510b-41b6-a4bc-6ee51a8d19a6');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '837B1E7D-4F84-4A21-B921-87EB6936F716');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '8e532415-6baf-42d5-9450-63c448c0d307');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '8e532415-6baf-42d5-9450-63c448c0d307');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '8f8e65b1-968b-4e47-a26f-466ab6e095b8');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '8f8e65b1-968b-4e47-a26f-466ab6e095b8');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '9809102d-91a5-4b6e-8a31-1f2ece3500d7');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', '9916879f-457d-4d66-bc82-5c584d601023');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', '9916879f-457d-4d66-bc82-5c584d601023');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', 'a9c06666-b0c6-4da3-b931-1e88f6e68568');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', 'a9c06666-b0c6-4da3-b931-1e88f6e68568');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', 'cd5ca696-b5ec-46cc-8f31-2752f82ab811');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', 'cd5ca696-b5ec-46cc-8f31-2752f82ab811');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', 'dc64a9e4-2f97-425b-9e8d-6c32c155094f');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', 'dc64a9e4-2f97-425b-9e8d-6c32c155094f');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', 'e3398957-89a8-4ac2-960a-0a80599d5da2');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', 'e3398957-89a8-4ac2-960a-0a80599d5da2');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', 'e49f7e9d-9ff9-4e86-b4a6-d08ecdb353eb');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', 'e49f7e9d-9ff9-4e86-b4a6-d08ecdb353eb');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', 'F7CEBEB8-DF52-41BD-8717-1F469CBE2A4B');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('admin', 'fe15070c-3466-4be9-a529-b15918ad34f8');
INSERT INTO checkIn.role_permission_mapping (role_type, permission_id) VALUES ('super admin', 'fe15070c-3466-4be9-a529-b15918ad34f8');
