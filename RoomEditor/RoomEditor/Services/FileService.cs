using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Windows.Storage;
using Newtonsoft.Json;
using RoomEditor.Model;

namespace RoomEditor.Services
{
    public class FileService
    {
        public async Task SaveRooms(int width, int height, IEnumerable<RoomSet> rooms)
        {
            var roomSetFile = new IORoomSetFile
            {
                Width = width,
                Height = height,
                RoomSets = rooms
                    .Select(roomSet =>
                        new IORoomSet
                        {
                            Id = roomSet.Id,
                            Name = roomSet.Name,
                            PresentRooms = roomSet.Rooms.Select(room => room.Present).ToArray()
                        }
                    )
                    .ToArray()
            };

            //Create the text file to hold the data
            var storageFolder = ApplicationData.Current.LocalFolder;
            var storageFile = await storageFolder.CreateFileAsync("eightyonerooms.json", CreationCollisionOption.ReplaceExisting);

            //Write data to the file
            await FileIO.WriteTextAsync(storageFile, JsonConvert.SerializeObject(roomSetFile, Formatting.Indented));
        }

        public async Task<IEnumerable<RoomSet>> LoadRooms()
        {
            //Create the text file to hold the data
            var storageFolder = ApplicationData.Current.LocalFolder;
            var storageFile = await storageFolder.GetFileAsync("eightyonerooms.json");

            //Write data to the file
            var fileContents = await FileIO.ReadTextAsync(storageFile);
            var ioRoomSetFile = JsonConvert.DeserializeObject<IORoomSetFile>(fileContents);

            var width = ioRoomSetFile.Width;
            var height = ioRoomSetFile.Height;

            return ioRoomSetFile.RoomSets.Select(_ => new RoomSet
            {
                Id = _.Id,
                Name = _.Name,
                Rooms = _.PresentRooms.Select((value, index) => new Room(index % width, index / height, value)).ToArray()
            });
        }
    }
}