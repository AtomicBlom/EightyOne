using System;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;

namespace RoomEditor.Framework.Converters
{
    public class RoomSelectedConverter : IValueConverter
    {
        public Brush Present { get; set; }
        public Brush NotPresent { get; set; }

        public object Convert(object value, Type targetType, object parameter, string language)
        {
            return (bool) value ? Present : NotPresent;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            return ((Brush) value == Present);
        }
    }

    public class RoomConnectedConverter : IValueConverter
    {
        public Brush Connected { get; set; }
        public Brush NotConnected { get; set; }
        public Brush NotValid { get; set; }

        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null) return NotValid;
            return (bool)value ? Connected : NotConnected;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            var brush = (Brush)value;
            if (brush == NotValid) return null;
            return  (brush == Connected);
        }
    }
}